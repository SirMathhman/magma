#ifndef magma_CompileException_h
#define magma_CompileException_h
struct CompileException extends Exception {
    public CompileException(String message, String context) {
        super(message + ": " + context);
    }

};
#endif