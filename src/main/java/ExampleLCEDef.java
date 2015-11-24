public class ExampleLCEDef
{
    //=====================================================//
    // Def enums
    //=====================================================//

    public enum MainStates
    {
        LOADING, CONTENT, ERROR
    }

    public enum Triggers
    {
        FORCE_LOADED;
    }

    //=====================================================//
    // Config FSM example
    //=====================================================//

    FsmEngine<MainStates, Triggers> mFsm = new FsmEngine<>();

    public ExampleLCEDef()
    {
        mFsm.newCylinder(MainStates.LOADING).setEnterAction(new LoadingEnterAction());
        mFsm.newCylinder(MainStates.CONTENT).setRequiredDataType(LoadedData.class);
        mFsm.newCylinder(MainStates.ERROR);
        mFsm.addTrigger(FsmEngine.Trigger.changeStatesOn(Triggers.FORCE_LOADED, MainStates.LOADING, MainStates.CONTENT));
        mFsm.start(MainStates.LOADING);

        //example trigger
        //mFsm.trigger(Triggers.FORCE_LOADED, new LoadedData("Forced data loaded by trigger"));
    }

    //=====================================================//
    // Getter
    //=====================================================//

    public FsmEngine<MainStates, Triggers> getFsm()
    {
        return mFsm;
    }

    //=====================================================//
    // State Actions
    //=====================================================//

    public static class LoadingEnterAction extends FsmEngine.Action<MainStates, Triggers>
    {
        @Override
        void run()
        {
            System.out.println("Running Loading!");

            //example of normal state transition
            getFsm().nextState(MainStates.CONTENT, new LoadedData("FakeLoadedData!"));
        }
    }

    //=====================================================//
    // State input data
    //=====================================================//

    public static class LoadedData extends FsmEngine.UnclassedDataType
    {
        public final String someLoadedData;

        public LoadedData(String someLoadedData)
        {
            this.someLoadedData = someLoadedData;
        }
    }
}

