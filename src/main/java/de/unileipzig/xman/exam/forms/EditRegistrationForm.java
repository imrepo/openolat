package de.unileipzig.xman.exam.forms;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * @author
 */
public class EditRegistrationForm extends FormBasicController {

	private DateChooser regStart;
	private DateChooser regEnd;
	private DateChooser signOff;
	private Submit submit;
	private Cancel cancel;

	/**
	 * creates the editRegistrationForm
	 * 
	 * @param name
	 *            the name of the form
	 * @param translator
	 *            the translator
	 * @param regStartDate
	 *            the start of the registration
	 * @param regEndDate
	 *            the end of the registratio
	 * @param signOffDate
	 *            the end of the unsubscription time
	 */
	public EditRegistrationForm(UserRequest ureq, WindowControl wControl,
			String name, Translator translator, Date regStartDate,
			Date regEndDate, Date signOffDate) {
		super(ureq, wControl);

		regStart = uifactory.addDateChooser("regStart",
				"EditRegistrationForm.regStartDate", SimpleDateFormat
						.getInstance().format(new Date()), null);
		regStart.setCustomDateFormat("dd.MM.yyyy HH:mm");
		regStart.setMandatory(true);
		regStart.setDisplaySize(20);
		regStart.setMaxLength(16);
		regStart.setDateChooserTimeEnabled(true);
		regStart.setDateChooserDateFormat("%d.%m.%Y %H:%M");
		if (regStartDate != null)
			regStart.setDate(regStartDate);
		regStart.setRegexMatchCheck(
				"\\d{2}\\.\\d{2}\\.\\d{4}\\ \\d{2}\\:\\d{2}",
				"EditRegistrationForm.isEmpty");

		regEnd = uifactory.addDateChooser("regEnd",
				"EditRegistrationForm.regEndDate", SimpleDateFormat
						.getInstance().format(new Date()), null);
		regEnd.setCustomDateFormat("dd.MM.yyyy HH:mm");
		regEnd.setMandatory(true);
		regEnd.setDisplaySize(20);
		regEnd.setMaxLength(16);
		regEnd.setDateChooserTimeEnabled(true);
		regEnd.setDateChooserDateFormat("%d.%m.%Y %H:%M");
		if (regEndDate != null)
			regEnd.setDate(regEndDate);
		regEnd.setRegexMatchCheck("\\d{2}\\.\\d{2}\\.\\d{4}\\ \\d{2}\\:\\d{2}",
				"EditRegistrationForm.isEmpty");

		signOff = uifactory.addDateChooser("signOff",
				"EditRegistrationForm.signOffDate", SimpleDateFormat
						.getInstance().format(new Date()), null);
		signOff.setCustomDateFormat("dd.MM.yyyy HH:mm");
		signOff.setMandatory(true);
		signOff.setExampleKey("EditRegistrationForm.signOffDate.example", null);
		signOff.setDisplaySize(20);
		signOff.setMaxLength(16);
		signOff.setDateChooserTimeEnabled(true);
		signOff.setDateChooserDateFormat("%d.%m.%Y %H:%M");
		if (signOffDate != null)
			signOff.setDate(signOffDate);
		signOff.setRegexMatchCheck(
				"\\d{2}\\.\\d{2}\\.\\d{4}\\ \\d{2}\\:\\d{2}",
				"EditRegistrationForm.isEmpty");

		// submit / cancel keys
		submit = uifactory.addFormSubmitButton("save", "saveButton", null);
		cancel = uifactory.addFormCancelButton("cancel", null, ureq,
				getWindowControl());
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see Form#validate()
	 */
	public boolean validateFormLogic(UserRequest ureq) {

		boolean validate = false;

		validate = !regStart.isEmpty("EditRegistrationForm.isEmpty")
				&& !regEnd.isEmpty("EditRegistrationForm.isEmpty")
				&& !signOff.isEmpty("EditRegistrationForm.isEmpty");

		if (validate) {

			if (regEnd.getDate().getTime() <= regStart.getDate().getTime()) {

				regEnd.setErrorKey("EditRegistrationForm.dateError", null);
				validate = false;
			}
			if (signOff.getDate().getTime() < regEnd.getDate().getTime()) {

				signOff.setErrorKey("EditRegistrationForm.dateError", null);
				validate = false;
			}
		}

		return validate;
	}

	/**
	 * @return a Date representing the start of the registration period
	 */
	public Date getRegStart() {

		return regStart.getDate();
	}

	/**
	 * @return a Date representing the end of the registration period
	 */
	public Date getRegEnd() {

		return regEnd.getDate();
	}

	/**
	 * @return a Date representing the sign off deadline
	 */
	public Date getSignOff() {

		return signOff.getDate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);

	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_FORM_CANCELLED);
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}
}