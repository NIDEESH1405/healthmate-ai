(function () {
  const departmentSelect = document.getElementById('department');
  const doctorSelect = document.getElementById('doctorName');
  const dataHolder = document.getElementById('departmentsData');

  let departments = [];
  try {
    departments = JSON.parse(dataHolder.getAttribute('data-departments') || '[]');
  } catch (e) {
    console.error('Failed to parse department data', e);
    departments = [];
  }

  function populateDoctors(deptName) {
    doctorSelect.innerHTML = '';
    const dept = departments.find(d => d.name === deptName);
    if (!dept) {
      const opt = document.createElement('option');
      opt.value = '';
      opt.textContent = 'Select department first';
      doctorSelect.appendChild(opt);
      return;
    }
    const placeholder = document.createElement('option');
    placeholder.value = '';
    placeholder.textContent = 'Select doctor';
    doctorSelect.appendChild(placeholder);

    dept.doctors.forEach(doc => {
      const opt = document.createElement('option');
      opt.value = doc.name;
      opt.textContent = doc.name + ' (' + doc.availability.join(', ') + ')';
      doctorSelect.appendChild(opt);
    });
  }

  departmentSelect.addEventListener('change', function () {
    populateDoctors(this.value);
  });

  // Restore selection state on validation-error re-render, if department was already chosen.
  if (departmentSelect.value) {
    populateDoctors(departmentSelect.value);
  }
})();
