package machine;

import desmoj.core.simulator.*;
import co.paralleluniverse.fibers.SuspendExecution;

// stellt die Schalteraktivitäten als Prozess dar
public class ServicePersonProcess extends SimProcess {

    // nuetzliche Referenz auf entsprechendes Modell
    private MachineRep_p_model meinModel;

    // Konstruktor
	  // Par 1: Modellzugehoerigkeit
	  // Par 2: Name des Ereignisses
	  // Par 3: show in trace?
    public ServicePersonProcess(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);

        meinModel = (MachineRep_p_model) owner;
    }
    
    // Beschreibung der Schalteraktivitaeten 
    public void lifeCycle() throws SuspendExecution {

        while (true){
            
            // keine Maschine kaputt
            if (meinModel.brokenMachineQueue.isEmpty()) {
                
                // Schalter in entsprechende WS
                meinModel.freeServicePersonQueue.insert(this);

                // abwarten weiterer Aktionen
                double timeBefore = meinModel.presentTime().getTimeAsDouble();
                passivate();
                double idleTime = meinModel.presentTime().getTimeAsDouble() - timeBefore;

                meinModel.idleServicePersonTime.update(idleTime);
                meinModel.idleCostTotal.update((idleTime / 60) * 10);
            }
            
            // Kunde wartet
            else {
                
                // erste Maschine aus WS entfernen
                MachineProcess machine = meinModel.brokenMachineQueue.first();
                meinModel.brokenMachineQueue.remove(machine);
                
                // Maschine wird repariert -> Prozess wird solange inaktiv gestellt
                double repairTime = meinModel.getRepairTime();
                hold(new TimeSpan(repairTime));
                meinModel.idleServicePersonTime.update(repairTime);
                meinModel.idleCostTotal.update((repairTime / 60) * 10);

                // Maschine wurde reapriert-> muss reaktiviert werden (kann weiterarbeiten)
                machine.activate(new TimeSpan(0.0));
            }
        }
    }
}
