import java.util.ArrayList;struct Splitter{
	void advance(any* _ref_, StringBuilder buffer, ArrayList<String> segments){
		if(!buffer.isEmpty())segments.add(buffer.toString());
	}
}