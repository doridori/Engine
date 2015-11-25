import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class FsmEngineTest
{
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
    // Triggers (+ Observers & Data Passing)
    //=====================================================//

    @Test
    public void trigger_validTriggerForCurrentState_shouldCallObserverWithNextState()
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

    @Test
    public void trigger_validTriggerPlusDataForCurrentState_shouldCallObserverWithNextStateAndData()
    {
        //setup
        FsmEngine<TestStates, TestTriggers> fsm = new FsmEngine<>();
        FsmEngine.Action mockEnterAction = Mockito.mock(FsmEngine.Action.class);
        fsm.defineCylinder(TestStates.ONE);
        fsm.defineCylinder(TestStates.TWO).setEnterAction(mockEnterAction).setRequiredDataType(String.class);
        fsm.defineTrigger(TestTriggers.TRIGGER_ONE, TestStates.ONE, TestStates.TWO);
        fsm.start(TestStates.ONE);

        //test
        Mockito.verify(mockEnterAction, Mockito.never()).run();
        String optionalInputData = "DummyStringData";
        fsm.trigger(TestTriggers.TRIGGER_ONE, optionalInputData);
        Mockito.verify(mockEnterAction, Mockito.times(1)).run();

        //test current state and data
        FsmEngine.Observer<TestStates> mockObserver = Mockito.mock(FsmEngine.Observer.class);
        fsm.addObserver(mockObserver);
        Mockito.verify(mockObserver).currentState(TestStates.TWO, optionalInputData);
    }

    @Test
    public void trigger_validIgnoreTriggerForCurrentState_shouldIgnore()
    {
        //setup
        FsmEngine<TestStates, TestTriggers> fsm = new FsmEngine<>();
        FsmEngine.Action mockEnterAction = Mockito.mock(FsmEngine.Action.class);
        fsm.defineCylinder(TestStates.ONE);
        fsm.defineCylinder(TestStates.TWO).setEnterAction(mockEnterAction);
        fsm.defineTrigger(TestTriggers.TRIGGER_ONE, TestStates.ONE, null); //ignore
        fsm.start(TestStates.ONE);

        //test
        Mockito.verify(mockEnterAction, Mockito.never()).run();
        fsm.trigger(TestTriggers.TRIGGER_ONE, null); //ignored

        //test current state
        FsmEngine.Observer<TestStates> mockObserver = Mockito.mock(FsmEngine.Observer.class);
        fsm.addObserver(mockObserver);
        Mockito.verify(mockObserver).currentState(TestStates.ONE, null);
    }

    @Test(expected = IllegalStateException.class)
    public void trigger_invalidTriggerForCurrentState_shouldThrow()
    {
        //setup
        FsmEngine<TestStates, TestTriggers> fsm = new FsmEngine<>();
        FsmEngine.Action mockEnterAction = Mockito.mock(FsmEngine.Action.class);
        fsm.defineCylinder(TestStates.ONE);
        fsm.defineCylinder(TestStates.TWO).setEnterAction(mockEnterAction);
        fsm.defineTrigger(TestTriggers.TRIGGER_ONE, TestStates.TWO, null);
        fsm.start(TestStates.ONE);

        //test
        Mockito.verify(mockEnterAction, Mockito.never()).run();
        fsm.trigger(TestTriggers.TRIGGER_ONE, null);
        Mockito.verify(mockEnterAction, Mockito.times(1)).run();
    }

    //=====================================================//
    // Race condition
    //=====================================================//

    @Test
    public void nextState_raceConditionsOnSwitchingStatesInsideEnterActions_AllEnterAndExitActionsCalledInCorrectOrderAndCorrectData()
    {
        //setup
        final FsmEngine<TestStates, FsmEngine.NoTriggers> fsm = new FsmEngine<>();

        //one
        FsmEngine.Action enterActionOne = new FsmEngine.Action() {
            @Override
            void run() {
                Assert.assertEquals(getOptionalInputData().getClass(), String.class);
                fsm.nextState(TestStates.TWO, new Integer(2));
            }
        };

        FsmEngine.Action exitActionOne = new FsmEngine.Action() {
            @Override
            void run() {
                Assert.assertEquals(getOptionalInputData().getClass(), String.class);
            }
        };

        FsmEngine.Action spyEnterActionOne = Mockito.spy(enterActionOne);
        FsmEngine.Action spyExitActionOne = Mockito.spy(exitActionOne);
        fsm.defineCylinder(TestStates.ONE).setEnterAction(spyEnterActionOne).setExitAction(spyExitActionOne).setRequiredDataType(String.class);

        //two
        FsmEngine.Action enterActionTwo = new FsmEngine.Action() {
            @Override
            void run() {
                Assert.assertEquals(getOptionalInputData().getClass(), Integer.class);
                fsm.nextState(TestStates.THREE, new Long(3));
            }
        };

        FsmEngine.Action exitActionTwo = new FsmEngine.Action() {
            @Override
            void run() {
                Assert.assertEquals(getOptionalInputData().getClass(), Integer.class);
            }
        };

        FsmEngine.Action spyEnterActionTwo = Mockito.spy(enterActionTwo);
        FsmEngine.Action spyExitActionTwo = Mockito.spy(exitActionTwo);
        fsm.defineCylinder(TestStates.TWO).setEnterAction(spyEnterActionTwo).setExitAction(spyExitActionTwo).setRequiredDataType(Integer.class);

        //three
        FsmEngine.Action mockEnterActionThree = Mockito.mock(FsmEngine.Action.class);
        fsm.defineCylinder(TestStates.THREE).setEnterAction(mockEnterActionThree).setRequiredDataType(Long.class);

        //start
        fsm.start(TestStates.ONE, "DummyInputString");

        //asserts
        InOrder inOrder = Mockito.inOrder(spyEnterActionOne, spyExitActionOne, spyEnterActionTwo, spyExitActionTwo, mockEnterActionThree);
        inOrder.verify(spyEnterActionOne).run();
        inOrder.verify(spyExitActionOne).run();
        inOrder.verify(spyEnterActionTwo).run();
        inOrder.verify(spyExitActionTwo).run();
        inOrder.verify(mockEnterActionThree).run();
    }

}