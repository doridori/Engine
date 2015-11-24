import java.util.HashMap;
import java.util.Map;

/**
 * Finite State Machine.
 *
 * {@link FsmEngine} is the FSM
 * {@link FsmEngine.Cylinder} represents the states
 * {@link FsmEngine.Trigger} represents allowed transitions
 *
 * All non-allowed transitions will throw once a trigger has been received.
 *
 * Very similar to other Java FSMs apart from you can pass an arbritrary data object around when firing triggers or when
 * triggering state transitions from inside state-based actions.
 *
 * @param <E> State enum type
 * @param <T> Trigger events. Use {@link FsmEngine.NoTriggers} if there are no external triggers.
 */
public class FsmEngine<E, T>
{
    //=====================================================//
    // Definition Fields
    //=====================================================//

    /**
     * The states of this fsm
     */
    private Map<E, Cylinder<E, T>> mCylinderMap = new HashMap<>();
    /**
     * Map of Trigger-events -> Map of fromStates -> Triggers
     */
    private Map<T, Map<E, Trigger<E, T>>> mTriggerMap = new HashMap<>();

    //=====================================================//
    // Operating Fields
    //=====================================================//

    private Cylinder<E, T> mCurrentCylinder;
    /**
     * May be null
     */
    private UnclassedDataType mCurrentCylindersData;
    private Observer<E> mObserver;

    //=====================================================//
    // Builder
    //=====================================================//

    /**
     * Keeps track of when started so FSM cannot be configured after this point.
     */
    private boolean mStarted;

    public FsmEngine<E, T> start(E startingState)
    {
        mStarted = true;
        nextState(startingState, null);
        return this;
    }

    //=====================================================//
    // Public interface
    //=====================================================//

    /**
     * Calls {@link #nextState(Object, UnclassedDataType)} with null input data.
     *
     * @param state
     */
    public final void nextState(E state)
    {
        nextState(state, null);
    }

    /**
     * Switch to next state. If you want to enforce rules on state switching use {@link #trigger(Object, UnclassedDataType)} instead.
     *
     * @param state state to move to
     * @param optionalInputData can be null. Will be passed to next states {@link Action} classed
     */
    public final void nextState(E state, UnclassedDataType optionalInputData)
    {
        if(mCurrentCylinder != null && mCurrentCylinder.exitAction != null)
            doAction(mCurrentCylinder.exitAction, mCurrentCylindersData);

        if(!mCylinderMap.containsKey(state))
            throw new NullPointerException(state.getClass().getName()+"."+state.toString()+" does not exist in map!");
        mCurrentCylinder = mCylinderMap.get(state);

        //data checking
        if(mCurrentCylinder.requiredDataType == null && optionalInputData != null)
            throw new IllegalStateException("Current state does not require any data whereas some has been passed: "+mCurrentCylinder.stateEnum+" | "+optionalInputData.getClass().getName());
        if(mCurrentCylinder.requiredDataType != null && optionalInputData == null)
                throw new IllegalStateException("Current state requires input data whereas none has been passed: "+mCurrentCylinder.stateEnum+" | "+mCurrentCylinder.requiredDataType.getName());

        mCurrentCylindersData = optionalInputData;
        if(mCurrentCylinder.enterAction != null)
            doAction(mCurrentCylinder.enterAction, mCurrentCylindersData);

        notifyObserver();
    }

    public void trigger(T triggerEnum, UnclassedDataType optionalInputData)
    {
        Map<E, Trigger<E, T>> triggersByEvent = mTriggerMap.get(triggerEnum);
        if(triggersByEvent == null)
            throw new NullPointerException("No Triggers exist for "+triggerEnum.toString());
        Trigger<E, T> trigger = triggersByEvent.get(mCurrentCylinder.stateEnum);

        if(trigger.toState == null)
            return; //represents a safe to ignore trigger state
        else
            nextState(trigger.toState, optionalInputData);
    }

    //=====================================================//
    // Private interface
    //=====================================================//

    /**
     * Execute (enter/exit) action
     *
     * @param action FsmEngine.Action to execute
     * @param input can be null
     */
    private void doAction(Action<E, T> action, UnclassedDataType input)
    {
        try
        {
            action.setInputData(input);
            action.setFsm(this); //ignore unchecked - its quite hard to pass cyclinders with actions of the wrong type due to the <> used for cylinder def when using the builder
            action.run();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add configuration time {@link FsmEngine.Cylinder} which represent FSM states. Cant call after {@link #start(Object)} has been called.
     *
     * @param cylinder
     * @return
     */
    private FsmEngine<E, T> addCylinder(Cylinder<E, T> cylinder)
    {
        if(mStarted)
            throw new IllegalStateException("Cant configure after already started!");
        mCylinderMap.put(cylinder.stateEnum, cylinder);
        return this;
    }

    /**
     * Add configuration time {@link FsmEngine.Trigger}. Cant call after {@link #start(Object)} has been called.
     *
     * @param trigger trigger def
     */
    public FsmEngine<E, T> addTrigger(Trigger<E, T> trigger)
    {
        if(mStarted)
            throw new IllegalStateException("Cant configure after already started!");

        Map<E, Trigger<E, T>> triggersForEvent = mTriggerMap.get(trigger.onTrigger);
        if(triggersForEvent == null)
        {
            triggersForEvent = new HashMap<>();
            mTriggerMap.put(trigger.onTrigger, triggersForEvent);
        }
        else
        {
            //check a trigger for this from-state does not already exist
            if(triggersForEvent.get(trigger.fromState) != null)
                throw new IllegalStateException("You have already defined a Trigger from this event/fromState combo");
        }

        triggersForEvent.put(trigger.fromState, trigger);
        return this;
    }

    //=====================================================//
    // Triggers
    //=====================================================//

    /**
     * External triggers. An event that triggers a state transition based upon the existing state.
     *
     * @param <E>
     * @param <T>
     */
    public static class Trigger<E, T>
    {
        private final E fromState;
        private final E toState;
        private final T onTrigger;

        /**
         * @param onTrigger
         * @param fromState
         * @param toState can be null. If null will do nothing when this trigger received for the passed state.
         */
        private Trigger(T onTrigger, E fromState, E toState)
        {
            this.onTrigger = onTrigger;
            this.fromState = fromState;
            this.toState = toState;
        }

        public static <E, T> Trigger<E, T> changeStatesOn(T onTrigger, E fromState, E toState)
        {
            return new Trigger<>(onTrigger, fromState, toState);
        }

        public static <E, T> Trigger<E, T> ignoreTriggerOn(T onTrigger, E atState)
        {
            return new Trigger<>(onTrigger, atState, null);
        }
    }

    /**
     * Default no triggers enum
     */
    public enum NoTriggers{}

    //=====================================================//
    // Unclassed Data Type
    //=====================================================//

    /**
     * General options when passing back state specific data
     *
     * 1. Use a Map / Bundle approach - runtime checked if data exists and KEY constants need to be defined. Not using due to extra key def needed.
     * 2. Use a compile-time checked approach. Not using as results in more verbose state definitions (i.e. a type method per state and state enumerating interface definitions as per the Visitor pattern). See https://github.com/doridori/Dynamohttps://github.com/doridori/Dynamo for an example of this. Has its places but not ideal for small state machines with minimal/no data per state.
     * 3. Casting, which will auto-cast out to the expected type (essentially a cast with slightly improved error reporting). Runtime checked. This lib uses this approach as the focus is a lean state-machine. Essentially shifts the implementation checking away from the compiler and into your tests. Most of time time I find there isn`t any data to pass so type safety was not worth the massive amounts of boilerplate!
     */
    @SuppressWarnings("unchecked")
    public static abstract class UnclassedDataType
    {
        public <T> T getAsType(Class<T> clazz)
        {
            try
            {
                return (T) this;
            }
            catch(ClassCastException e)
            {
                throw new RuntimeException("Trying to cast "+this.getClass().getName()+" to "+clazz.getName());
            }
        }
    }

    //=====================================================//
    // Cylinder class
    //=====================================================//

    /**
     * Create a new Typed Cylinder
     * @return
     */
    public Cylinder<E, T> newCylinder(E stateEnum)
    {
        Cylinder<E, T> newCylinder = new Cylinder<>(stateEnum);
        addCylinder(newCylinder);
        return newCylinder;
    }

    /**
     * Represents a state definition
     *
     * @param <E> State enum type
     * @param <T> Trigger type
     */
    public static class Cylinder<E, T>
    {
        private final E stateEnum;
        private Action<E, T> enterAction;
        private Action<E, T> exitAction;
        private Class<?> requiredDataType;

        private Cylinder(E stateEnum)
        {
            this.stateEnum = stateEnum;
        }

        private E getStateEnum() {
            return stateEnum;
        }

        public Action<E, T> getEnterAction() {
            return enterAction;
        }

        public Action<E, T> getExitAction() {
            return exitAction;
        }

        public Cylinder<E,T> setEnterAction(Action<E, T> enterAction)
        {
            this.enterAction = enterAction;
            return this;
        }

        public Cylinder<E,T> setExitAction(Action<E, T> exitAction)
        {
            this.exitAction = exitAction;
            return this;
        }

        /**
         * Optional. If this Cylinders {@link Action} classes are expected a input data type you can specify
         * it here. If this state is attempted to be created and the type was not passed an exception will be thrown.
         *
         * @param requiredDataType
         * @return
         */
        public Cylinder<E,T> setRequiredDataType(Class<?> requiredDataType)
        {
            this.requiredDataType = requiredDataType;
            return this;
        }
    }

    //=====================================================//
    // FsmEngine.Action class
    //=====================================================//

    /**
     * Actions are defined at Engine definition time and represent Enter/Exit actions for a state.
     *
     * Retain no-arg constructor as created with reflection. This means the class will need to be public also (and not a non-static inner class)
     */
    public abstract static class Action<E, T>
    {
        private UnclassedDataType unclassedDataType;
        private FsmEngine<E, T> mFsm;

        /**
         * Optional data object that can be used by this action
         *
         * @param unclassedDataType can be null
         */
        void setInputData(UnclassedDataType unclassedDataType)
        {
            this.unclassedDataType = unclassedDataType;
        }

        /**
         * @return Data associated with the containing state - may be null
         */
        public UnclassedDataType getUnclassedDataType()
        {
            return unclassedDataType;
        }

        public FsmEngine<E, T> getFsm() {
            return mFsm;
        }

        void setFsm(FsmEngine<E, T> fsm) {
            this.mFsm = fsm;
        }

        abstract void run();
    }

    //=====================================================//
    // Observable
    //=====================================================//

    public interface Observer<E>
    {
        /**
         * @param state state enum
         * @param unclassedDataType may be null - should be documented as part of fsm implementation
         */
        void currentState(E state, UnclassedDataType unclassedDataType);
    }

    /**
     * @param observer will be notified instantly if the fsm has some state.
     */
    public void addObserver(Observer<E> observer)
    {
        this.mObserver = observer;
        notifyObserver();
    }

    private void notifyObserver()
    {
        if(mObserver != null && mCurrentCylinder != null)
            mObserver.currentState(mCurrentCylinder.stateEnum, mCurrentCylindersData);
    }


}
