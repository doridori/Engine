import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class FsmEngineTest
{
    //TODO test state transtions (with and without data)
    //TODO test triggers (positive, negative, throws, ignores, with data)
    //TODO test error paths
    //TODO test race conditions

    enum TestStates
    {
        ONE, TWO, THREE;
    }

    @Test
    public void nextState_enterActionDefined_shouldCallAction()
    {
        //setup
        FsmEngine<TestStates, FsmEngine.NoTriggers> fsm = new FsmEngine<>();
        FsmEngine.Action mockEnterAction = Mockito.mock(FsmEngine.Action.class);
        fsm.newCylinder(TestStates.ONE).setEnterAction(mockEnterAction);
        fsm.newCylinder(TestStates.TWO);
        fsm.start(TestStates.ONE);

        //test
        Mockito.verify(mockEnterAction).run();
    }

}