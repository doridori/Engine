Engine
======

An observable Finite State Machine for Java.

Similar to other FSMs out there. This has been designed with a terse interface in mind and also supports optional arbritrary data objects for each `Cylinder` (state), which can be accessed via a `Cylinders` `Actions` (enter / exit). 

Simple to use. See the [tests](https://github.com/doridori/Engine/blob/master/src/test/java/com/kodroid/engine/FsmEngineTest.java) for some examples

```java
FsmEngine<TestStates, TestTriggers> fsm = new FsmEngine<>();

fsm.defineCylinder(TestStates.ONE);

FsmEngine.Action mockEnterAction = Mockito.mock(FsmEngine.Action.class);
fsm.defineCylinder(TestStates.TWO)
    .setEnterAction(mockEnterAction)
    .setRequiredDataType(String.class);
    
FsmEngine.Action mockTriggerAction = Mockito.mock(FsmEngine.Action.class);
fsm.defineTrigger(TestTriggers.TRIGGER_ONE, TestStates.ONE)
    .setAction(mockTriggerAction)
    .setRequiredDataType(Integer.class);
    
fsm.start(TestStates.ONE);
```

Usage
=====

```gradle
...
repositories {
    jcenter()
}
...

dependencies {
    compile 'com.kodroid:engine:0.9.4’
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
