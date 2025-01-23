import java.util.ArrayList;struct Splitter{
	void advance(StringBuilder buffer, ArrayList<String> segments){
		if(!buffer.isEmpty())segments.add(buffer.toString());
	}
	struct Splitter new(){
		struct Splitter this;
		return this;
	}
}