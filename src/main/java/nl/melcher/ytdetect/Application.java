package nl.melcher.ytdetect;

import nl.melcher.ytdetect.nfa.Nfa;
import nl.melcher.ytdetect.nfa.NfaFactory;
import nl.melcher.ytdetect.nfa.NfaState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Application {

	public static void main(String[] args) {
		VideoIdentifier videoGangnam = new VideoIdentifier("PSY - GANGNAM STYLE(강남스타일) M/V",
				1080, 0, "https://www.youtube.com/watch?v=9bZkp7q19f0");
		Integer[] videoGangnamSizes = {
				1024607,66247,1023520,96126,2097152,216230,2097152,1988348,378763,1892826,
				1706490,1893946,1668170,313398,1779779,1914012,2065584,1778899,330164,1785246,
				1886280,1754357,321297,1734149,2097152,2097152,276514,2097152,1490822,1684719,
				361391,1789515,1865257,1831108,2097152,2097152,327793,2097152,2097152,1943785,
				329175,1850533,1784270,2097152,2097152,333260,1622310,1923691,1891091,2089677,
				318495,1896358,2097152,2097152,345735,1881400,1829998,1917128
		};


		NfaFactory factory = new NfaFactory();
		factory.addVideo(videoGangnam, Arrays.asList(videoGangnamSizes));

		Nfa nfa = factory.build();
		nfa.next(2097152).next(333260).next(1622310);

		System.out.println("curstate:");
		for(NfaState state : nfa.getGlobalState()) {
			System.out.println(state.getVideoIdentifier().getTitle());
		}

		nfa.next(543);
		System.out.println("curstate:");
		for(NfaState state : nfa.getGlobalState()) {
			System.out.println(state.getVideoIdentifier().getTitle());
		}
	}
}
