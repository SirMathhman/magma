#include "./State.h"
struct State(int depth){
	boolean isShallow(){
		return depth==1;
	}
}
