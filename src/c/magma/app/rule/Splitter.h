import java.util.ArrayList;struct Splitter{
	struct Table{
		void advance(StringBuilder buffer, ArrayList<String> segments){
			if(!buffer.isEmpty())segments.add(buffer.toString());
		}
	}
	struct Impl{}
}