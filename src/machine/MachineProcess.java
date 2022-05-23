package machine;

import desmoj.core.simulator.*;
import co.paralleluniverse.fibers.SuspendExecution;

// stellt die Kundenaktivitaeten als Prozess dar
public class MachineProcess extends SimProcess {

    // nuetzliche Referenz auf entsprechendes Modell
    private MachineRep_p_model meinModel;

    // Konstruktor
	  // Par 1: Modellzugehoerigkeit
	  // Par 2: Name des Ereignisses
	  // Par 3: show in trace?
	  // Par 3: show in trace?
    public MachineProcess(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);

        meinModel = (MachineRep_p_model) owner;
    }

    
    // Beschreibung der Aktionen des Kunden vom Eintreffen bis zum Verlassen
    // des Schalters
    public void lifeCycle() throws SuspendExecution {

        while (true) {
            hold(new TimeSpan(meinModel.getUpTime()));

            // Maschine wird defekt -> in die Warteschlange geben
            meinModel.brokenMachineQueue.insert(this);

            sendTraceNote("# of Broken machines: " +
                    meinModel.brokenMachineQueue.length());

            // Schalter frei?
            if (!meinModel.freeServicePersonQueue.isEmpty()) {
                // Schalter frei, von entsprechender WS holen
                ServicePersonProcess schalter = meinModel.freeServicePersonQueue.first();
                // extra Entfernen von WS notwendig
                meinModel.freeServicePersonQueue.remove(schalter);

                // Schalter sofort als naechsten Prozess aktivieren
                schalter.activateAfter(this);

                // Bedienvorgang ueber sich ergehen lassen
                passivate();
            }
            // Schalter besetzt
            else {
                // Maschine wartet in der WS
                double timeBefore = meinModel.presentTime().getTimeAsDouble();
                passivate();
                double idleTime = meinModel.presentTime().getTimeAsDouble() - timeBefore;

                meinModel.idleMachineTime.update(idleTime);
                meinModel.idleCostTotal.update((idleTime / 60) * 50);
            }

            // Kunde wurde bedient und verlaesst den Schalterraum
            //  -> in diesem Beispiel nur eine Meldung sinnvoll
            sendTraceNote("Maschine was fixed");
        }
    }
}
