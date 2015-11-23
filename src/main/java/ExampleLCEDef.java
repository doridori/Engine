public class ExampleLCEDef
{
    public enum MainStates
    {
        LOADING, CONTENT, ERROR
    }

    FsmEngine<MainStates, FsmEngine.NoTriggers> mFsm;

    public ExampleLCEDef()
    {
         mFsm = new FsmEngine.Builder<MainStates, FsmEngine.NoTriggers>()
                .addCylinder(new FsmEngine.Cylinder<MainStates, FsmEngine.NoTriggers>(MainStates.LOADING, LoadingEnterAction.class))
                .addCylinder(new FsmEngine.Cylinder<MainStates, FsmEngine.NoTriggers>(MainStates.CONTENT))
                .addCylinder(new FsmEngine.Cylinder<MainStates, FsmEngine.NoTriggers>(MainStates.ERROR))
                .setStartingState(MainStates.LOADING)
                .build();
    }

    public FsmEngine<MainStates, FsmEngine.NoTriggers> getFsm()
    {
        return mFsm;
    }

    public static class LoadingEnterAction extends FsmEngine.Action<MainStates, FsmEngine.NoTriggers>
    {
        @Override
        void run()
        {
            //do some loading
            getFsm().nextState(MainStates.CONTENT, new LoadedData("FakeLoadedData!"));
        }
    }

    public static class LoadedData extends FsmEngine.UnclassedDataType
    {
        public final String someLoadedData;

        public LoadedData(String mSomeLoadedData)
        {
            this.someLoadedData = mSomeLoadedData;
        }
    }
}

