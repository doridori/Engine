

public class ExampleLCEConsumer implements FsmEngine.Observer<ExampleLCEDef.MainStates>
{
    public static final void main(String[] args)
    {
        new ExampleLCEConsumer();
    }

    public ExampleLCEConsumer()
    {
        new ExampleLCEDef().getFsm().addObserver(this);
    }

    @Override
    public void currentState(ExampleLCEDef.MainStates state, FsmEngine.UnclassedDataType unclassedDataType)
    {
        switch (state)
        {
            case LOADING:
                System.out.println("loading");
                break;
            case CONTENT:
                System.out.println(unclassedDataType.getAsType(ExampleLCEDef.LoadedData.class).someLoadedData);
                break;
            case ERROR:
                break;
        }
    }
}
