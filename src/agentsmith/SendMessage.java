/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentsmith;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author Ethan_Hunt
 */
public class SendMessage extends OneShotBehaviour {
        
    private ACLMessage msg;

    private AgentSmith myAgent;

    public SendMessage(ACLMessage msg,AgentSmith myAgent){
        super();
        this.msg = msg;
        this.myAgent = myAgent;
    }

    @Override
    public void action() {
        myAgent.send(msg);
        System.out.println("****I Sent Message to::>  *****"+"\n"+
                            "The Content of My Message is::>"+ msg.getContent());

    }
}



