#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
struct StripRule {
	Rule childRule;
	struct StripRule StripRule_new(Rule childRule){
		struct StripRule this;
		this.childRule = childRule;
		return this;
	}
	CompileError> StripRule_apply(void* _this_){
		struct StripRule this = *(struct StripRule*) this;
		return this.childRule.apply(input.strip());
	}
};