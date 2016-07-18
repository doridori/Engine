[![Circle CI](https://circleci.com/gh/doridori/Engine.svg?style=svg)](https://circleci.com/gh/doridori/Engine)

Engine
======

An observable Finite State Machine for Java.

Useful when you have a number of distinct states, with some optional state transitions, and associated actions. 

For example, you may have states `ON_CALL` and `OFF_CALL`, with triggers `START_CALL` and `END_CALL`, with an assocated action to print the call length when `END_CALL` is received. 

Terminology
===========

| Term    | Is a |
| :------ | :------------------- |
| Engine  | Finite State Machine |
| Cylinder| State      |
| Trigger | Event      |
| Action  | Action     |

About
======

- Like other popular FSMs (e.g. [Stateless4j](https://github.com/oxo42/stateless4j)) but also designed with the below in mind.
- Terse interface. Define only what you need.
- Supports optional arbritrary data objects which `Action` can access if defined as a `Trigger` action or a `Cylinders` enter/exit action.
- Defensive. Will throw if a trigger is received for a state where it has not been pre-defined.

Example
=======

Simple to use. See the [tests](https://github.com/doridori/Engine/blob/master/src/test/java/com/kodroid/engine/FsmEngineTest.java) for some examples

```java
FsmEngine<CallStates, CallTriggers> fsm = new FsmEngine<>();

//define first state with no associated actions
fsm.defineCylinder(CallStates.OFF_CALL);

//define second state with an associated ENTER action that requires a String input
FsmEngine.Action mockEnterAction = Mockito.mock(FsmEngine.Action.class);
fsm.defineCylinder(CallStates.ON_CALL)
    .setEnterAction(mockEnterAction) //mock action can access the passed String
    .setRequiredDataType(String.class);
    
//define a tigger action that can only be received when the FSM is in state ONE. Requires an Integer input.
FsmEngine.Action mockTriggerAction = Mockito.mock(FsmEngine.Action.class);
fsm.defineTrigger(CallTriggers.END_CALL, CallStates.ON_CALL)
    .setAction(mockTriggerAction) //mock action can access the passed Integer
    .setRequiredDataType(Integer.class);
    
//set the initial state of the machine
fsm.start(CallStates.OFF_CALL);
```

Runtime Type Checking
=====================

This library does use some runtime checking of passed data types (as opposed to compile time). This is intentional as it vastly reduces the amount of code needed to create the FSM (i.e. dont need to employ extra objects / visitor pattern / interfaces to support compile time checking for optional state-specific data). The risk of this approach is that programming errors are not exposed till runtime. I feel this is justified for this library as:

1. Your tests should pick up any issues of runtime type mismatch
2. The amount this increased code readablility (due to less LOCs) in this case makes the code easier to grok, therefore bugs are less likely to appear!

From a Java perspective this feels slightly strange as the mindset is generally "If your casting your doing it wrong" but for this lib I feel its the right design decision. This shifts the responsibility of code-stability one step away from the compiler and one towards your test suite.

Usage
=====

```gradle
...
repositories {
    jcenter()
}
...

dependencies {
    compile 'com.kodroid:engine:0.9.5’
}
```

License
=======

    Copyright 2015 Dorian Cussen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
