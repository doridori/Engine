# Engine

A Finite State Machine for Java

Simple to use. See the tests for some examples

```java
FsmEngine<TestStates, TestTriggers> fsm = new FsmEngine<>();
FsmEngine.Action mockEnterAction = Mockito.mock(FsmEngine.Action.class);
fsm.defineCylinder(TestStates.ONE);
fsm.defineCylinder(TestStates.TWO).setEnterAction(mockEnterAction).setRequiredDataType(String.class);
fsm.defineTrigger(TestTriggers.TRIGGER_ONE, TestStates.ONE, TestStates.TWO);
fsm.start(TestStates.ONE);
```

