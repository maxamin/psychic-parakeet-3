import {FormControl} from '@angular/forms';

export function validateRequired(control: FormControl) {
  let valid = true;
  if (!control.value) {
    valid = false;
  }
  if (typeof control.value === 'string') {
    valid = control.value.trim().length > 0;
  }
  if (typeof  control.value === 'number') {
    valid = true;
  }
  return valid ? null : {
    required: {
      valid: false
    }
  };
}
