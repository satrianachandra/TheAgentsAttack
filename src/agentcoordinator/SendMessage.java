/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentcoordinator;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author Ethan_Hunt
 */
public class SendMessage extends OneShotBehaviour {
        
        private ACLMessage msg;
        
        private AgentCoordinator myAgent;
        
        public SendMessage(ACLMessage msg,AgentCoordinator myAgent){
            super();
            this.msg = msg;
            this.myAgent = myAgent;
        }
        
        @Override
        public void action() {
            myAgent.send(msg);
            System.out.println("****I Sent Message to::> R1 *****"+"\n"+
                                "The Content of My Message is::>"+ msg.getContent());
            
            //GuiEvent ge = new GuiEvent(this, AgentCommGUI.MESSAGE_SENT);
            //ge.addParameter(msg);
            //myAgent.postGuiEvent(ge);
        }
    }

