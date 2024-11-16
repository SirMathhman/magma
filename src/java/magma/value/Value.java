package magma.value;

import magma.Mnemonic;
import magma.option.Option;

public interface Value {
    Option<Mnemonic> findMnemonic();
}
