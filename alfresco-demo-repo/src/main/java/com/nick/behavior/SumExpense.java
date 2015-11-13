package com.nick.behavior;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.alfresco.model.ContentModel;

import com.nick.model.MyCoBehaviorModel;
import com.nick.model.MyCoModel;

public class SumExpense implements NodeServicePolicies.OnCreateChildAssociationPolicy, NodeServicePolicies.OnDeleteChildAssociationPolicy{

	
	private NodeService nodeService;
	private PolicyComponent policyComponent;
	
	// Behaviours
    private Behaviour onCreateChildAssociation;
    private Behaviour onDeleteChildAssociation;
    
	private Logger logger = Logger.getLogger(SumExpense.class);
	
	public void init() {
		if (logger.isDebugEnabled()){
			logger.debug("Initializing SumExpense behavior");
		}
		
		//Create behaviours
		this.onCreateChildAssociation = new JavaBehaviour(this, "onCreateChildAssociation" , Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
		this.onDeleteChildAssociation = new JavaBehaviour(this, "onDeleteChildAssociation" , Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
		
		
		this.policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"),
				ContentModel.TYPE_FOLDER,
				this.onCreateChildAssociation);
		this.policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteChildAssociation"),
				ContentModel.TYPE_FOLDER,
				this.onDeleteChildAssociation);
	}
	

	@Override
	public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean arg1) {
		if (logger.isDebugEnabled()){
			logger.debug("Inside onCreateChildAssociation");
		}
		computeSum(childAssocRef);
	}
	
	@Override
	public void onDeleteChildAssociation(ChildAssociationRef childAssocRef) {
		if (logger.isDebugEnabled()){
			logger.debug("Inside onDeleteChildAssociation");
		}
		computeSum(childAssocRef);
	}
				
	public void computeSum(ChildAssociationRef childAssocRef){
		if (logger.isDebugEnabled()){
			logger.debug("Inside computeSum");
		}
		
		//get the parent node
		NodeRef parentRef = childAssocRef.getParentRef();
		QName parentType = nodeService.getType(parentRef);
		
		QName expenseFolder = QName.createQName(MyCoBehaviorModel.NAMESPACE_MYCO_BEHAVIOR_MODEL, MyCoBehaviorModel.TYPE_MYB_EXPENSE_FOLDER);
		QName expenseReport = QName.createQName(MyCoModel.NAMESPACE_MYCO_CONTENT_MODEL, MyCoModel.TYPE_MY_EXPENSE_REPORT);
		QName totalAmount = QName.createQName(MyCoModel.NAMESPACE_MYCO_CONTENT_MODEL, MyCoModel.PROP_MY_TOTAL_AMOUNT);
		QName expenseSum = QName.createQName(MyCoBehaviorModel.NAMESPACE_MYCO_BEHAVIOR_MODEL, MyCoBehaviorModel.PROP_MYB_EXPENSE_SUM);
		
		if (parentType.isMatch(expenseFolder)){
			if (logger.isDebugEnabled()){
				logger.debug("Parent is an expense folder");
			}
			
			logger.debug("getting parent's children");
			
			List<ChildAssociationRef> children = nodeService.getChildAssocs(parentRef);
			
			Double expenseTotal = 0.0;
			logger.debug("expense sum: should be 0.0: " + expenseSum);
			
			if (children.size() == 0){
				if (logger.isDebugEnabled()){
					logger.debug("No children found.");
				}
			} else {
				if (logger.isDebugEnabled()){
					logger.debug("Children found.");
				}
				for (ChildAssociationRef child : children){
					logger.debug("In children loop");
					
					NodeRef childRef = child.getChildRef();
					QName childType = nodeService.getType(childRef);
					
					if (childType.isMatch(expenseReport)){
						logger.debug("child is expense report");
						Double expenseVal = (Double) nodeService.getProperty(childRef, totalAmount);
						expenseTotal += expenseVal;
						logger.debug("ExpenseSum: " + expenseTotal + " ExpenseVal: " + expenseVal);
					}
				}
				
				Map<QName, Serializable> parentProps = nodeService.getProperties(parentRef);
				parentProps.put(expenseSum, expenseTotal);
				
				nodeService.setProperties(parentRef, parentProps);
				
			}
			
			
		}
		
		return;
	}
	
	public NodeService getNodeService() {
		return nodeService;
	}


	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	public PolicyComponent getPolicyComponent() {
		return policyComponent;
	}


	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
}











