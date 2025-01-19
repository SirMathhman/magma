

struct ApplicationError {
    Rc_Error cause;
};

ApplicationError ApplicationError_new(Rc_Error cause){
    struct ApplicationError this;
    this.cause = cause;
    return this;
}

Rc_String ApplicationError_display(void* _this_) {
    Rc_ApplicationError this = *(Rc_ApplicationError*) this);
    Rc_Error _local0_ = this.cause;
    return _local0_.display(&_local0_);
}
