import java.util.ArrayList;
struct Splitter {
	static void advance(StringBuilder buffer, ArrayList<String> segments){
		if(!buffer.isEmpty())segments.add(buffer.toString());
	}
}

