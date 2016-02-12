package universityOfEdinburg.semanticWebSystems;

import java.util.List;

import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

public class GeoNamesClient {

	public GeoNamesClient() {

		WebService.setUserName("s1568644");
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static int getBestMatch(String name) {
		int bestMatch = -1;

		try {
			ToponymSearchResult searchResult = search(name);
			for (Toponym toponym : searchResult.getToponyms()) {
				if (toponym.getName().contains(name)) {
					if (toponym.getFeatureCode().equals("ADM3")) {
						System.out.println("Best match: " + toponym.getName() + "ID: " + toponym.getGeoNameId());
						bestMatch = toponym.getGeoNameId();
						if (parentContainsName("Greater London", toponym.getGeoNameId())) {
							System.out
									.println("Top Best match: " + toponym.getName() + "ID: " + toponym.getGeoNameId());
							bestMatch = toponym.getGeoNameId();
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bestMatch;
	}

	/**
	 * 
	 * @param name
	 * @param id
	 * @return
	 */
	public static boolean parentContainsName(String name, int id) {
		List<Toponym> parents = getParents(id);
		for (Toponym top : parents) {
			if (top.getName().equals(name))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static List<Toponym> getParents(int id) {
		try {
			return WebService.hierarchy(id, "en", Style.SHORT);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	public static ToponymSearchResult search(String query) {
		ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();

		try {
			searchCriteria.setCountryCode("UK");
			searchCriteria.setQ(query);
			searchCriteria.setStyle(Style.FULL);

			return WebService.search(searchCriteria);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		new GeoNamesClient();
	}

}