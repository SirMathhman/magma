package magma.value;

import magma.Mnemonic;
import magma.option.Option;
import magma.option.Some;

public record MnemonicValue(Mnemonic mnemonic) implements Value {
    @Override
    public Option<Mnemonic> findMnemonic() {
        return new Some<>(mnemonic);
    }
}
