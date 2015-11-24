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
    //TODO remove example classes

    enum TestStates
    {
        ONE, TWO, THREE;
    }

    enum TestTriggers
    {
        TRIGGER_ONE;
    }

    //=====================================================//
    // State Actions
    //=====================================================//

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

    @Test
    public void nextState_exitActionDefined_shouldCallAction()
    {
        //setup
        FsmEngine<TestStates, FsmEngine.NoTriggers> fsm = new FsmEngine<>();
        FsmEngine.Action mockExitAction = Mockito.mock(FsmEngine.Action.class);
        fsm.newCylinder(TestStates.ONE).setExitAction(mockExitAction);
        fsm.newCylinder(TestStates.TWO);
        fsm.start(TestStates.ONE);

        //test
        Mockito.verifyZeroInteractions(mockExitAction);
        fsm.nextState(TestStates.TWO);
        Mockito.verify(mockExitAction).run();
    }

    //=====================================================//
    // Triggers
    //=====================================================//

    @Test
    public void trigger_validTrigger_shouldCallObserverWithNextState()
    {
        //setup
        FsmEngine<TestStates, TestTriggers> fsm = new FsmEngine<>();
        FsmEngine.Action mockEnterAction = Mockito.mock(FsmEngine.Action.class);
        fsm.newCylinder(TestStates.ONE);
        fsm.newCylinder(TestStates.TWO).setEnterAction(mockEnterAction);
        fsm.addTrigger(new FsmEngine.Trigger<TestStates, TestTriggers>()) //todo improve trigger declaration so creates object internally with correct typing
        fsm.start(TestStates.ONE);

        //test
        Mockito.verify(mockEnterAction).run();
    }

}