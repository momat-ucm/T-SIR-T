package exec;

import general.Country;

public class RunItaly_VariantsVaccines {

	public static void main(String[] args) {
		
		Country Ita1var0vac = new Country("Italy\\2");
		
		Ita1var0vac.evaluatepost();
		
		Country Ita2var0vac = new Country("Italy\\3");
		
		Ita2var0vac.evaluatepost();
		
		Country Ita1var2vac = new Country("Italy\\4");
		
		Ita1var2vac.evaluatepost();
		
		Country Ita2var2vac = new Country("Italy\\5");
		
		Ita2var2vac.evaluatepost();
		
	}

}
