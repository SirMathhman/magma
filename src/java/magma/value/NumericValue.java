package magma.value;

import magma.Mnemonic;
import magma.option.None;
import magma.option.Option;

public class NumericValue implements Value {
    @Override
    public Option<Mnemonic> findMnemonic() {
        return new None<>();
    }
}
