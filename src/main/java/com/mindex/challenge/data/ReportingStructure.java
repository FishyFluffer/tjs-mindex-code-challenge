package com.mindex.challenge.data;

import com.mindex.challenge.dao.EmployeeRepository;

public class ReportingStructure {
    private Employee employee;
    private int numberOfReports;

    public ReportingStructure(){
    }

    public ReportingStructure(Employee employee, EmployeeRepository employeeRepository) {
        this.employee = employee;
        this.numberOfReports = calcNumOfReports(employee, employeeRepository);
    }

    private int calcNumOfReports(Employee employee, EmployeeRepository employeeRepository) {
        // case for employee not found, ignore for now
        if(employee == null) { return 0; }

        // case for no direct reports, nothing to add
        if(employee.getDirectReports() == null || employee.getDirectReports().isEmpty())
            return 0;

        // get total reports from all direct reports
        int subReports = employee.getDirectReports()
                .stream()
                .mapToInt(e -> calcNumOfReports(
                        employeeRepository.findByEmployeeId( e.getEmployeeId()),
                        employeeRepository))
                .sum();

        // finally, add in the direct reports themselves
        return subReports + employee.getDirectReports().size();
    }

    public Employee getEmployee() {
        return employee;
    }

    public int getNumberOfReports() {
        return numberOfReports;
    }

    public void setEmployee(Employee employee) { this.employee = employee; }

    public void setNumberOfReports(int num) { this.numberOfReports = num; }
}
