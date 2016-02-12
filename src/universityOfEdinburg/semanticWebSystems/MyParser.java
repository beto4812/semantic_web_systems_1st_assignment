package universityOfEdinburg.semanticWebSystems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class MyParser {

	Map<String, Integer> boroughGeoNameId = new HashMap<>();
	Map<String, String> namespaces = new HashMap<>();
	String religions[] = { "Christian", "Buddhist", "Hindu", "Jewish", "Muslim", "Sikh", "Other religion", "Atheism",
			"" };
	GeoNamesClient c = new GeoNamesClient();
	Model m = ModelFactory.createDefaultModel();
	int rowId = 0;
	int popId = 0;
	PrintWriter writer = null;
	Resource temp;

	public static void main(String[] args) {

		new MyParser().parseCSV();

	}

	public void init() {
		namespaces.put("gn", "http://sws.geonames.org/");
		namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		namespaces.put("foaf", "http://xmlns.com/foaf/spec/");
		namespaces.put("my", "http://vocab.inf.ed.ac.uk/sws/s1568644/");
		namespaces.put("db", "http://dbpedia.org/resource/");
	}

	public MyParser() {
		init();
	}

	public void parseCSV() {

		try {
			writer = new PrintWriter("ola.ttl", "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		prefixes();
		myDefinitions();
		mainContent();

		m.write(writer, "Turtle");
		writer.close();
	}

	public void prefixes() {

		Set<String> it = namespaces.keySet();
		for (String temp : it) {
			writer.println("@prefix " + temp + ": " + namespaces.get(temp));
		}
	}

	public void myDefinitions() {
		// Area Code
		temp = m.createResource("my:areaCode");
		temp.addProperty(m.createProperty("rdf:type"), m.createProperty("rdfs:Property"));
		temp.addProperty(m.createProperty("rdfs:label"), "London area code of a feature");
		temp.addProperty(m.createProperty("rdfs:domain"), m.createProperty("gn:Feature"));

		// Population Percentage
		temp = m.createResource("my:populationPercentage");
		temp.addProperty(m.createProperty("rdf:type"), "rdfs:Property");
		temp.addProperty(m.createProperty("rdfs:label"), "Population percentage of a group");
		temp.addProperty(m.createProperty("rdfs:domain"), "foaf:Group");
	}

	/**
	 * 
	 */
	public void mainContent() {
		BufferedReader br = null;
		String line = null;
		try {
			br = new BufferedReader(new FileReader("religion-ward-2001.csv"));
			line = br.readLine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean firstLine = true;

		while (line != null) {

			rowId++;

			try {
				line = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (line == null) {
				break;
			}

			System.out.println(line);

			String parts[] = line.split(",");

			if (!firstLine) {
				System.out.println("parts[1]: " + parts[1]);
				if (boroughGeoNameId.get(parts[1]) == null) {
					boroughGeoNameId.put(parts[1], c.getBestMatch(parts[1]));
					if (boroughGeoNameId.get(parts[1]) != -1) {
						temp = m.createResource("geo:" + boroughGeoNameId.get(parts[1]));
						temp.addProperty(m.createProperty("rdfs:label"), parts[1]);
					}
				}

				temp = m.createResource("my:A" + rowId);

				temp.addProperty(m.createProperty("rdfs:label"), parts[2]);
				temp.addProperty(m.createProperty("rdf:type"), m.createResource("gn:Feature"));
				temp.addProperty(m.createProperty("my:areaCode"), parts[0]);

				for (int i = 0; i <= 8; i++) {
					createPopulationResource(i, parts[4 + i], parts[13 + i]);
					temp.addProperty(m.createProperty("foaf:Group"), m.createResource("my:P" + popId));
				}
				if (boroughGeoNameId.get(parts[1]) != -1) {
					temp.addProperty(m.createProperty("gn:parentFeature"),
							m.createResource("gn:" + boroughGeoNameId.get(parts[1])));
				}

				temp.addProperty(m.createProperty("gn:population"), parts[3]);
			}
			firstLine = false;
		}
	}

	/**
	 * 
	 * @param population
	 * @param percentage
	 */
	public void createPopulationResource(int relIndex, String population, String percentage) {

		Resource pop = m.createResource();
		popId++;
		pop = m.createResource("P" + popId);

		if (religions[relIndex] != "") {
			pop.addProperty(m.createProperty("db:religion"), m.createResource("db:" + religions[relIndex]));
		} else {
			pop.addProperty(m.createProperty("db:religion"), "");
		}

		pop.addProperty(m.createProperty("my:populationPercentage"), percentage);
		pop.addProperty(m.createProperty("gn:population"), population);
	}

}
