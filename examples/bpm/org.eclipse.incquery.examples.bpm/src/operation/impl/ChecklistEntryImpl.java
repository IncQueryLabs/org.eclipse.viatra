/**
 */
package operation.impl;

import java.util.Collection;

import operation.ChecklistEntry;
import operation.MenuItem;
import operation.OperationPackage;
import operation.RuntimeInformation;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import process.Task;
import system.Job;
import org.eclipse.incquery.querybasedfeatures.runtime.IQueryBasedFeatureHandler;
import org.eclipse.incquery.querybasedfeatures.runtime.QueryBasedFeatureKind;
import org.eclipse.incquery.querybasedfeatures.runtime.QueryBasedFeatureHelper;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Checklist Entry</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link operation.impl.ChecklistEntryImpl#getMenu <em>Menu</em>}</li>
 *   <li>{@link operation.impl.ChecklistEntryImpl#getInfo <em>Info</em>}</li>
 *   <li>{@link operation.impl.ChecklistEntryImpl#getTaskId <em>Task Id</em>}</li>
 *   <li>{@link operation.impl.ChecklistEntryImpl#getJobPaths <em>Job Paths</em>}</li>
 *   <li>{@link operation.impl.ChecklistEntryImpl#getTask <em>Task</em>}</li>
 *   <li>{@link operation.impl.ChecklistEntryImpl#getJobs <em>Jobs</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ChecklistEntryImpl extends OperationElementImpl implements ChecklistEntry {
	/**
	 * The cached value of the '{@link #getMenu() <em>Menu</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMenu()
	 * @generated
	 * @ordered
	 */
	protected MenuItem menu;

	/**
	 * The cached value of the '{@link #getInfo() <em>Info</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInfo()
	 * @generated
	 * @ordered
	 */
	protected RuntimeInformation info;

	/**
	 * The default value of the '{@link #getTaskId() <em>Task Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTaskId()
	 * @generated
	 * @ordered
	 */
	protected static final String TASK_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTaskId() <em>Task Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTaskId()
	 * @generated
	 * @ordered
	 */
	protected String taskId = TASK_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getJobPaths() <em>Job Paths</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJobPaths()
	 * @generated
	 * @ordered
	 */
	protected EList<String> jobPaths;

	/**
	 * The cached setting delegate for the '{@link #getTask() <em>Task</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTask()
	 * @generated
	 * @ordered
	 */
	protected EStructuralFeature.Internal.SettingDelegate TASK__ESETTING_DELEGATE = ((EStructuralFeature.Internal)OperationPackage.Literals.CHECKLIST_ENTRY__TASK).getSettingDelegate();

	/**
	 * The cached setting delegate for the '{@link #getJobs() <em>Jobs</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJobs()
	 * @generated
	 * @ordered
	 */
	protected EStructuralFeature.Internal.SettingDelegate JOBS__ESETTING_DELEGATE = ((EStructuralFeature.Internal)OperationPackage.Literals.CHECKLIST_ENTRY__JOBS).getSettingDelegate();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ChecklistEntryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OperationPackage.Literals.CHECKLIST_ENTRY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MenuItem getMenu() {
		if (menu != null && menu.eIsProxy()) {
			InternalEObject oldMenu = (InternalEObject)menu;
			menu = (MenuItem)eResolveProxy(oldMenu);
			if (menu != oldMenu) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OperationPackage.CHECKLIST_ENTRY__MENU, oldMenu, menu));
			}
		}
		return menu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MenuItem basicGetMenu() {
		return menu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMenu(MenuItem newMenu) {
		MenuItem oldMenu = menu;
		menu = newMenu;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OperationPackage.CHECKLIST_ENTRY__MENU, oldMenu, menu));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RuntimeInformation getInfo() {
		return info;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetInfo(RuntimeInformation newInfo, NotificationChain msgs) {
		RuntimeInformation oldInfo = info;
		info = newInfo;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, OperationPackage.CHECKLIST_ENTRY__INFO, oldInfo, newInfo);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInfo(RuntimeInformation newInfo) {
		if (newInfo != info) {
			NotificationChain msgs = null;
			if (info != null)
				msgs = ((InternalEObject)info).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - OperationPackage.CHECKLIST_ENTRY__INFO, null, msgs);
			if (newInfo != null)
				msgs = ((InternalEObject)newInfo).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - OperationPackage.CHECKLIST_ENTRY__INFO, null, msgs);
			msgs = basicSetInfo(newInfo, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OperationPackage.CHECKLIST_ENTRY__INFO, newInfo, newInfo));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTaskId() {
		return taskId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTaskId(String newTaskId) {
		String oldTaskId = taskId;
		taskId = newTaskId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OperationPackage.CHECKLIST_ENTRY__TASK_ID, oldTaskId, taskId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getJobPaths() {
		if (jobPaths == null) {
			jobPaths = new EDataTypeUniqueEList<String>(String.class, this, OperationPackage.CHECKLIST_ENTRY__JOB_PATHS);
		}
		return jobPaths;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Task getTask() {
		return (Task)TASK__ESETTING_DELEGATE.dynamicGet(this, null, 0, true, false);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Task basicGetTask() {
		return (Task)TASK__ESETTING_DELEGATE.dynamicGet(this, null, 0, false, false);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<Job> getJobs() {
		return (EList<Job>)JOBS__ESETTING_DELEGATE.dynamicGet(this, null, 0, true, false);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case OperationPackage.CHECKLIST_ENTRY__INFO:
				return basicSetInfo(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case OperationPackage.CHECKLIST_ENTRY__MENU:
				if (resolve) return getMenu();
				return basicGetMenu();
			case OperationPackage.CHECKLIST_ENTRY__INFO:
				return getInfo();
			case OperationPackage.CHECKLIST_ENTRY__TASK_ID:
				return getTaskId();
			case OperationPackage.CHECKLIST_ENTRY__JOB_PATHS:
				return getJobPaths();
			case OperationPackage.CHECKLIST_ENTRY__TASK:
				if (resolve) return getTask();
				return basicGetTask();
			case OperationPackage.CHECKLIST_ENTRY__JOBS:
				return getJobs();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case OperationPackage.CHECKLIST_ENTRY__MENU:
				setMenu((MenuItem)newValue);
				return;
			case OperationPackage.CHECKLIST_ENTRY__INFO:
				setInfo((RuntimeInformation)newValue);
				return;
			case OperationPackage.CHECKLIST_ENTRY__TASK_ID:
				setTaskId((String)newValue);
				return;
			case OperationPackage.CHECKLIST_ENTRY__JOB_PATHS:
				getJobPaths().clear();
				getJobPaths().addAll((Collection<? extends String>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case OperationPackage.CHECKLIST_ENTRY__MENU:
				setMenu((MenuItem)null);
				return;
			case OperationPackage.CHECKLIST_ENTRY__INFO:
				setInfo((RuntimeInformation)null);
				return;
			case OperationPackage.CHECKLIST_ENTRY__TASK_ID:
				setTaskId(TASK_ID_EDEFAULT);
				return;
			case OperationPackage.CHECKLIST_ENTRY__JOB_PATHS:
				getJobPaths().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case OperationPackage.CHECKLIST_ENTRY__MENU:
				return menu != null;
			case OperationPackage.CHECKLIST_ENTRY__INFO:
				return info != null;
			case OperationPackage.CHECKLIST_ENTRY__TASK_ID:
				return TASK_ID_EDEFAULT == null ? taskId != null : !TASK_ID_EDEFAULT.equals(taskId);
			case OperationPackage.CHECKLIST_ENTRY__JOB_PATHS:
				return jobPaths != null && !jobPaths.isEmpty();
			case OperationPackage.CHECKLIST_ENTRY__TASK:
				return TASK__ESETTING_DELEGATE.dynamicIsSet(this, null, 0);
			case OperationPackage.CHECKLIST_ENTRY__JOBS:
				return JOBS__ESETTING_DELEGATE.dynamicIsSet(this, null, 0);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (taskId: ");
		result.append(taskId);
		result.append(", jobPaths: ");
		result.append(jobPaths);
		result.append(')');
		return result.toString();
	}

} //ChecklistEntryImpl
