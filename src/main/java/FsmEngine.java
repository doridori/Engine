import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Finite State Machine
 *
 * @param <E> State enum type
 * @param <T> Trigger events. Use {@link FsmEngine.NoTriggers} if there are no external triggers.
 */
public class FsmEngine<E, T>
{
    //=====================================================//
    // Definition Fields
    //=====================================================//

    private Map<E, Cylinder<E>> mCylinderMap = new HashMap<>();
    private Map<T, Trigger> mTriggerMap = new HashMap<>();

    //=====================================================//
    // Operating Fields
    //=====================================================//

    private Cylinder<E> mCurrentCylinder;
    /**
     * May be null
     */
    private UnclassedDataType mCurrentCylindersData;
    private Observer<E> mObserver;

    //=====================================================//
    // Builder
    //=====================================================//

    /**
     * @param <E> State enum type
     * @param <T> Trigger events
     */
    public static class Builder<E, T>
    {
        private final FsmEngine<E, T> mFsmEngine;
        private boolean mStarted;

        public Builder()
        {
            mFsmEngine = new FsmEngine<E, T>();
        }

        public Builder<E, T> addCylinder(Cylinder<E> cylinder)
        {
            mFsmEngine.addCylinder(cylinder);
            return this;
        }

        public Builder<E, T> addTrigger(Trigger<E, T> trigger)
        {
            mFsmEngine.addTrigger(trigger);
            return this;
        }

        public Builder<E, T> setStartingState(E startingState)
        {
            mStarted = true;
            mFsmEngine.nextState(startingState, null);
            return this;
        }

        public FsmEngine<E, T> build()
        {
            if(!mStarted)
                throw new IllegalStateException("Not started");
            return mFsmEngine;
        }
    }

    //=====================================================//
    // Public interface
    //=====================================================//

    /**
     * Switch to next state. If you want to enforce rules on state switching use {@link #trigger(Object, UnclassedDataType)} instead.
     *
     * @param state
     * @param optionalInputData
     */
    public final void nextState(E state, UnclassedDataType optionalInputData)
    {
        if(mCurrentCylinder != null && mCurrentCylinder.exitActionClass != null)
            doAction(mCurrentCylinder.exitActionClass, mCurrentCylindersData);

        mCurrentCylinder = mCylinderMap.get(state);
        mCurrentCylindersData = optionalInputData;
        if(mCurrentCylinder.enterActionClass != null)
            doAction(mCurrentCylinder.enterActionClass, mCurrentCylindersData);

        notifyObserver();
    }

    public void trigger(T triggerEnum, UnclassedDataType optionalInputData)
    {
        //needs to get all trigger objects that can be used to move from the current state, enumerate them to see if any triggerEnums match, then perform the next state or throw
        throw new NotImplementedException();
    }

    //=====================================================//
    // Private interface
    //=====================================================//

    /**
     * Execute (enter/exit) action
     *
     * @param actionClass
     * @param input
     */
    private void doAction(Class<? extends Action> actionClass, UnclassedDataType input)
    {
        try
        {
            Action action = actionClass.getConstructor().newInstance();
            action.setInputData(input);
            action.setFsm(this); //ignore unchecked - its quite hard to pass cyclinders with actions of the wrong type due to the <> used for cylinder def when using the builder
            action.run();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    private void addCylinder(Cylinder<E> cylinder)
    {
        mCylinderMap.put(cylinder.stateEnum, cylinder);
    }

    /**
     * Only public interface once running!
     *
     * @param trigger
     */
    private void addTrigger(Trigger<E, T> trigger)
    {
        mTriggerMap.put(trigger.onTrigger, trigger);
    }

    //=====================================================//
    // Triggers
    //=====================================================//

    /**
     * External triggers
     *
     * @param <E>
     * @param <T>
     */
    public static class Trigger<E, T>
    {
        private final E fromState;
        private final E toState;
        private final T onTrigger;

        public Trigger(E fromState, E toState, T onTrigger)
        {
            this.fromState = fromState;
            this.toState = toState;
            this.onTrigger = onTrigger;
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
     * Represents a state definition
     *
     * @param <E> State enum type
     */
    public static class Cylinder<E>
    {
        private final E stateEnum;
        private final Class<? extends Action> enterActionClass;
        private final Class<? extends Action> exitActionClass;

        public Cylinder(E stateEnum)
        {
            this(stateEnum, null, null);
        }

        public Cylinder(E stateEnum, Class<? extends Action> enterActionClass)
        {
            this(stateEnum, enterActionClass, null);
        }

        public Cylinder(E stateEnum, Class<? extends Action> enterActionClass, Class<? extends Action> exitActionClass)
        {
            this.stateEnum = stateEnum;
            this.enterActionClass = enterActionClass;
            this.exitActionClass = exitActionClass;
        }
    }

    //=====================================================//
    // Action class
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
         * @param unclassedDataType
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

        public void setFsm(FsmEngine<E, T> fsm) {
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
         * @param state
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
