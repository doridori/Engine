public class ExampleLCEDef
{
    public enum MainStates
    {
        LOADING, CONTENT, ERROR
    }

    FsmEngine<MainStates, FsmEngine.NoTriggers> mFsm = new FsmEngine<>();

    public ExampleLCEDef()
    {
         mFsm.newCylinder(MainStates.LOADING).setEnterActionClass(LoadingEnterAction.class);
         mFsm.newCylinder(MainStates.CONTENT);
         mFsm.newCylinder(MainStates.ERROR);
         mFsm.start(MainStates.LOADING);
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

