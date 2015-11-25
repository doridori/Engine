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
        fsm.defineCylinder(TestStates.ONE).setEnterAction(mockEnterAction);
        fsm.defineCylinder(TestStates.TWO);
        fsm.start(TestStates.ONE);

        //test
        Mockito.verify(mockEnterAction, Mockito.times(1)).run();
    }

    @Test
    public void nextState_exitActionDefined_shouldCallAction()
    {
        //setup
        FsmEngine<TestStates, FsmEngine.NoTriggers> fsm = new FsmEngine<>();
        FsmEngine.Action mockExitAction = Mockito.mock(FsmEngine.Action.class);
        fsm.defineCylinder(TestStates.ONE).setExitAction(mockExitAction);
        fsm.defineCylinder(TestStates.TWO);
        fsm.start(TestStates.ONE);

        //test
        Mockito.verifyZeroInteractions(mockExitAction);
        fsm.nextState(TestStates.TWO);
        Mockito.verify(mockExitAction, Mockito.times(1)).run();
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
        fsm.defineCylinder(TestStates.ONE);
        fsm.defineCylinder(TestStates.TWO).setEnterAction(mockEnterAction);
        fsm.defineTrigger(TestTriggers.TRIGGER_ONE, TestStates.ONE, TestStates.TWO);
        fsm.start(TestStates.ONE);

        //test
        Mockito.verify(mockEnterAction, Mockito.never()).run();
        fsm.trigger(TestTriggers.TRIGGER_ONE, null);
        Mockito.verify(mockEnterAction, Mockito.times(1)).run();
    }

}