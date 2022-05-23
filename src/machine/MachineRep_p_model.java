package machine;

import desmoj.core.simulator.*;
import desmoj.core.dist.*;
import desmoj.core.statistic.Aggregate;

/*
 main-Klasse vom einfachen Schalter-Modell (abgeleitet von 
 desmoj.core.simulator.Model) - stellt die notwendige Infrastruktur zur Verfuegung
*/


public class MachineRep_p_model extends Model {
    public final int MACHINES = 10;
    public final int SERVICEPERSONS = 8;

	// Zufallszahlengenerator fuer die Dauer bis zum Defekt einer Maschine
	private ContDistExponential upTime;

    public double getUpTime() {
	   return upTime.sample();
    }

    // Zufallszahlengenerator zur Ermittlung der RepairTime am Schalter
	private ContDistExponential repairTime;

    public double getRepairTime() {
        return repairTime.sample();
    }

    // Aggregator für Zeit von ausgefallenen Maschinen
    protected Aggregate idleMachineTime;

    // Aggegator für Zeit von wartenden Servicepersonen
    protected Aggregate idleServicePersonTime;

    // Aggegator für die Kosten für wartende Servicepersonen und Maschinen
    public Aggregate idleCostTotal;

	/**
    * Warteschlange fuer wartende Kunden
    * jeder Kunde kommt zuerst hier hinein
    *
    * liefert elementare Statistik
    */
    protected ProcessQueue<MachineProcess> brokenMachineQueue;


	/*
    * Warteschlange fuer freie Schalter
    * -> elementare Statistik erhaeltlich
    * -> mehrere Schalter koennen verwaltet werden
	*/
	protected ProcessQueue<ServicePersonProcess> freeServicePersonQueue;
	

     // Konstruktor
    public MachineRep_p_model(Model owner, String name, boolean showInReport,
                            boolean showIntrace) {
    	super(owner, name, showInReport, showIntrace);
    }

     // Kurzbeschreibung des Modells
    public String description() {
    	return "Schalter2_p Model (Prozess orientiert)l:" +
               "simuliert einen Bankschalter, wo ankommende Kunden zuerst in einer"+
               "Warteschlange eingereiht werden. Wenn der Schalter frei ist,"+
               "werden sie bedient.";
    }	


    // erste Ereignisse eintragen f�r Simulationsbeginn
    public void doInitialSchedules() {

        for(int m = 0; m < MACHINES; m++) {
        // Maschinen einrichten und starte
            MachineProcess newMachine = new MachineProcess (this, "Maschine", true);
            newMachine.activate(new TimeSpan(0.0));
        }

        // ServicePersonen einrichten und starten
        for(int s = 0; s < SERVICEPERSONS; s++) {
            ServicePersonProcess servicePerson = new ServicePersonProcess(this, "ServicePerson", true);
            servicePerson.activate(new TimeSpan(0.0));
        }

    }


    // Initialisierung der benutzten DESMO-J Infrastruktur
    public void init() {
		
    	// Generator fuer Ankunftszeiten initialisieren
    	// Par 1: Modellzugehoerigkeit
    	// Par 2: Name des Generators
    	// Par 3: mittlere Zeitdauer in Minuten zwischen Kundenankuenften
    	// Par 4: show in report?
    	// Par 5: show in trace?
        upTime = new ContDistExponential(this, "machine-uptime", 480, true, true);

    	// negative Ankunftszeitintervalle sind nicht moeglich, 
    	// jedoch liefert Exponentialverteilung auch negative Werte, daher
        upTime.setNonNegative(true);

    	// Generator fuer Bedienzeiten initialisieren
    	// Par 1: Modellzugehoerigkeit
    	// Par 2: Name des Generators
    	// Par 3: minimale Bedienzeit in Minuten
    	// Par 4: maximale Bedienzeit in Minuten
    	// Par 5: show in report?
    	// Par 6: show in trace?
        repairTime = new ContDistExponential(this, "machine-repair-time", 120, true, true);

    	// Warteschlange fuer Kunden initialisieren
    	// Par 1: Modellzugehoerigkeit
    	// Par 2: Name der Warteschlange
    	// Par 3: show in report?
    	// Par 4: show in trace?
       	brokenMachineQueue = new ProcessQueue<MachineProcess>(this, "BrokenMachineQueue",true, true);
	
    	// Warteschlange fuer freie Schalter initialisieren
    	// Par 1: Modellzugehoerigkeit
    	// Par 2: Name der Warteschlange
    	// Par 3: show in report?
    	// Par 4: show in trace?
        freeServicePersonQueue = new ProcessQueue<ServicePersonProcess>(this, "FreeServicePersonQueue",true, true);

        idleMachineTime = new Aggregate(this, "idleMachineTime (min)", true, true);

        idleServicePersonTime = new Aggregate(this, "idleServicePersonTime (min)", true, true);

        idleCostTotal = new Aggregate(this, "totalCost (Eur)", true, true);
    }

    // Hauptmethode, zustaendig fuer
    // - Experiment instantieren
    // - Modell instantieren
    // - Modell mit Experiment verbinden
    //   - Einstellungen fuer Simulation und Ergebnisberichte
    //   - Simulation starten
    //   - Kriterium fuer Simulationsende aufstellen
    //   - Reports initialisieren
    //   - aufraeumen, abschliessen
    public static void main(java.lang.String[] args) {
    	// neues Experiment erzeugen
    	// ATTENTION!
    	// Use as experiment name a OS filename compatible string!!
    	// Otherwise your simulation will crash!!
    	Experiment machineExperiment =
            new Experiment("MachineRep-Prozess");

        // neues Modell erzeugen
        // Par 1: null markiert main model, sonst Mastermodell angeben
        MachineRep_p_model sch_p_model =
            new MachineRep_p_model(null, "MachineRep Modell", true, true);

        // Modell mit Experiment verbinden
        sch_p_model.connectToExperiment(machineExperiment);

        // Intervall fuer trace/debug
        machineExperiment.tracePeriod(new TimeInstant(0.0), new TimeInstant(120));
        machineExperiment.debugPeriod(new TimeInstant(0.0), new TimeInstant(120) );

        // Ende der Simulation setzen
        // -> hier 4 Stunden (= 240 min)
        machineExperiment.stop(new TimeInstant(4320));

        // Experiment zur Zeit 0.0 starten
        machineExperiment.start();

        // -> Simulation laeuft bis Abbruchkriterium erreicht ist
        // -> danach geht es hier weiter

        // Report generieren
        machineExperiment.report();

        // Ausgabekanaele schliessen, allfaellige threads beenden
        machineExperiment.finish();

    }
}
