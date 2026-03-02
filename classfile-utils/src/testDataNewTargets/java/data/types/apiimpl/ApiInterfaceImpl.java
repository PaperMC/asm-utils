package data.types.apiimpl;

public class ApiInterfaceImpl implements ApiInterface {

    @Override
    public int get() {
        return 0;
    }

    // leave method because bytecode rewriting uses it
    public String get0() {
        return "rewritten";
    }
}
