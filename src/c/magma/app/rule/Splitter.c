import java.util.ArrayList;
public struct Splitter {
	void advance=void advance(StringBuilder buffer, ArrayList<String> segments){
		if(!buffer.isEmpty())segments.add(buffer.toString());
	};
}