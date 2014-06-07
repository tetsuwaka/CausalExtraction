package cabochaParser;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class ReversedTest {

	@Test
	public void reversedTest1() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		int i = 3;
		for (Integer num : Reversed.reversed(list)) {
			assertThat(i, is(num));
			i -= 1;
		}
	}

	@Test
	public void reversedTest2() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("abc");
		list.add("efg");
		list.add("hij");
		
		ArrayList<String> list2 = new ArrayList<String>();
		list2.add("hij");
		list2.add("efg");
		list2.add("abc");
		
		int i = 0;
		for (String str : Reversed.reversed(list)) {
			assertThat(str, is(list2.get(i)));
			i += 1;
		}
	}

}
