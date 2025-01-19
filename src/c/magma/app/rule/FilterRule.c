#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
struct FilterRule {
	Predicate<String> filter;
	Rule childRule;
	struct FilterRule FilterRule_new(Predicate<String> filter, Rule childRule){
		struct FilterRule this;
		this.filter = filter;
		this.childRule = childRule;
		return this;
	}
	CompileError> FilterRule_apply(void* _this_){
		struct FilterRule this = *(struct FilterRule*) this;
		temp = temp;
		temp = temp;
	}
};