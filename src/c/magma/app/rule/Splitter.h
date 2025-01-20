import java.util.ArrayList;
public struct Splitter {
	((StringBuilder, ArrayList<String>) => void) advance=void advance(StringBuilder buffer, ArrayList<String> segments){
		if(!buffer.isEmpty())segments.add(buffer.toString());
	};
}