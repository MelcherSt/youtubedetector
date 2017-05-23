package nl.melcher.ytdetect;

import nl.melcher.ytdetect.nfa.NfaFactory;
import nl.melcher.ytdetect.nfa.SuperNfa;

import java.util.Arrays;


public class Application {

	public static void main(String[] args) {
		VideoIdentifier videoGangnam = new VideoIdentifier("PSY - GANGNAM STYLE(강남스타일) M/V",
				1080, 0, "https://www.youtube.com/watch?v=9bZkp7q19f0");

		VideoIdentifier vidFake1 = new VideoIdentifier("Fake video 1",
				1080, 0, "https://www.youtube.com/watch?v=9bZkp7q19f0");

		VideoIdentifier vidFake2 = new VideoIdentifier("Fake video 2",
				1080, 0, "https://www.youtube.com/watch?v=9bZkp7q19f0");

		VideoIdentifier vidFake3 = new VideoIdentifier("Fake video 3",
				1080, 0, "https://www.youtube.com/watch?v=9bZkp7q19f0");

		VideoIdentifier vidFake4 = new VideoIdentifier("Fake video 4",
				1080, 0, "https://www.youtube.com/watch?v=9bZkp7q19f0");

		Integer[] videoGangnamSizes = {
				1024607,66247,1023520,96126,2097152,216230,2097152,1988348,378763,1892826,
				1706490,1893946,1668170,313398,1779779,1914012,2065584,1778899,330164,1785246,
				1886280,1754357,321297,1734149,2097152,2097152,276514,2097152,1490822,1684719,
				361391,1789515,1865257,1831108,2097152,2097152,327793,2097152,2097152,1943785,
				329175,1850533,1784270,2097152,2097152,333260,1622310,1923691,1891091,2089677,
				318495,1896358,2097152,2097152,345735,1881400,1829998,1917128
		};

		Integer[] mocks1 = {144059, 365881, 228181, 304020, 200429, 313699, 92771, 258460,
				79991, 146113, 366081, 261303, 223138, 230927, 159965, 125034, 328382, 264698,
				166556, 336701, 67493, 91136, 323337, 342284, 370483, 110154, 377211, 300763,
				161717, 284040, 177347, 96223, 331632, 352790, 340874, 267772, 98246, 99252, 171339,
				89845, 102179, 399680, 51149, 197776, 102273, 214923, 58765, 128231, 73436, 193844
		};

		Integer[] mocks2 = {230783, 55386, 98277, 153900, 377062, 196930, 120635, 338747, 92377,
				354029, 359437, 274689, 320829, 380410, 138731, 356914, 241029, 94422, 75693, 242195,
				174548, 240828, 256842, 107027, 317973, 247886, 317952, 347834, 58363, 90850, 379040,
				123559, 243226, 61636, 85614, 309272, 346876, 104504, 355858, 86727, 168286, 358279,
				211716, 142632, 355748, 66309, 133578, 398308, 67030, 72251
		};

		Integer[] mocks3 = {
				1893946,1668170,313398,1779779,1914012,2065584,1778899,330164,1785246,
				1886280,1754357,321297,1734149,2097152,2097152,276514,2097152,1490822,1684719,
				361391, 1789515, 55386, 98277, 153900, 377062, 196930, 120635, 338747, 92377,
				354029, 359437, 274689, 320829, 380410, 138731, 356914, 241029, 94422, 75693, 242195,
				174548, 240828, 256842, 107027
		};

		Integer[] mocks4 = {
				321297,1734149,2097152,2097152,276514,2097152,1490822,1684719,
				361391, 1789515, 55386, 98277, 153900, 377062, 196930, 120635, 338747, 92377,
				354029, 359437, 144059, 365881, 228181, 304020, 200429, 313699, 92771, 258460,
				79991, 146113, 366081, 261303, 223138, 230927, 159965, 125034, 328382, 264698,
				166556, 336701, 67493
		};

		NfaFactory factory = new NfaFactory();
		factory.addVideo(videoGangnam, Arrays.asList(videoGangnamSizes));
		factory.addVideo(vidFake1, Arrays.asList(mocks1));
		factory.addVideo(vidFake2, Arrays.asList(mocks2));
		factory.addVideo(vidFake3, Arrays.asList(mocks3));
		factory.addVideo(vidFake4, Arrays.asList(mocks4));

		SuperNfa nfa = new SuperNfa(factory.build());
		nfa.next(1779779).next(354029).next(359437).next(144059).next(365881).next(228181).next(304020);

		/*System.out.println("curstate:");
		for(NfaState state : nfa.getGlobalState()) {
			System.out.println(state.getVideoIdentifier().getTitle());
		}

		nfa.next(543);
		System.out.println("curstate:");
		for(NfaState state : nfa.getGlobalState()) {
			System.out.println(state.getVideoIdentifier().getTitle());
		}*/
	}
}
