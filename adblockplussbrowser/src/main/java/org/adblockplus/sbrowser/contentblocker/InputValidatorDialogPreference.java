/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.sbrowser.contentblocker;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.adblockplus.adblockplussbrowser.R;
import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.util.PreferenceUtils;
import org.apache.commons.validator.routines.DomainValidator;

public class InputValidatorDialogPreference extends EditTextPreference implements TextWatcher,
    TextView.OnEditorActionListener
{

  public enum ValidationType
  {
    DOMAIN,
    URL
  }

  private static final String TAG = InputValidatorDialogPreference.class.getSimpleName();
  private OnInputReadyListener onInputReadyListener;
  private AlertDialog alertDialog;
  private ValidationType validationType;

  public InputValidatorDialogPreference(Context context)
  {
    this(context, null);
  }

  public InputValidatorDialogPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    // Setting defaults
    this.setIcon(android.R.drawable.ic_menu_add);
    this.setPositiveButtonText(android.R.string.ok);
    this.setNegativeButtonText(android.R.string.cancel);
    final EditText editText = getEditText();
    editText.addTextChangedListener(this);
    editText.setOnEditorActionListener(this);
    editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
  }

  @Override
  protected void showDialog(Bundle state)
  {
    super.showDialog(state);

    alertDialog = (AlertDialog) getDialog();
    // Positive button is disabled until a valid URL is entered
    this.setPositiveButtonEnabled(false);
  }

  @Override
  protected void onDialogClosed(boolean positiveResult)
  {
    super.onDialogClosed(positiveResult);

    alertDialog = null;
    if (positiveResult && this.onInputReadyListener != null)
    {
      this.onInputReadyListener.onInputReady(getInput());
    }
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after)
  {
    // Ignored
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count)
  {
    // Ignored
  }

  @Override
  public void afterTextChanged(Editable s)
  {
    setPositiveButtonEnabled(isValidInput());
  }

  @Override
  public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
  {
    if (actionId == EditorInfo.IME_ACTION_DONE)
    {
      if (this.isValidInput())
      {
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
      }
      else
      {
        Toast.makeText(getContext(), R.string.whitelist_website_invalid_url_error, Toast.LENGTH_SHORT).show();
      }
      return true;
    }
    return false;
  }

  @Override
  protected void onBindView(View view)
  {
    super.onBindView(view);
    PreferenceUtils.setMultilineTitle(view);
  }

  public void setValidationType(@NonNull final ValidationType validationType)
  {
    this.validationType = validationType;
  }

  public void setOnInputReadyListener(OnInputReadyListener listener)
  {
    this.onInputReadyListener = listener;
  }

  private boolean isValidDomain()
  {
    return DomainValidator.getInstance().isValid(getInput());
  }

  private boolean isValidUrl()
  {
    return Patterns.WEB_URL.matcher(getInput()).matches();
  }

  private boolean isValidInput()
  {
    if (validationType == null)
    {
      Log.i(TAG, "ValidationType was not defined and is therefore set to" +
          " ValidationType.URL per default. Please consider to set in manually.");
      validationType = ValidationType.URL;
    }

    switch (validationType)
    {
      case DOMAIN:
        return isValidDomain();
      default:
        return isValidUrl();
    }
  }

  private String getInput()
  {
    return getEditText().getText().toString();
  }

  private void setPositiveButtonEnabled(boolean enabled)
  {
    if (alertDialog != null)
    {
      alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(enabled);
    }
  }

  public interface OnInputReadyListener
  {
    void onInputReady(String input);
  }
}