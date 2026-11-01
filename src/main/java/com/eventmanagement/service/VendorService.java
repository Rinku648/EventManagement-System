package com.eventmanagement.service;

import com.eventmanagement.dao.VendorDAO;
import com.eventmanagement.model.Vendor;
import com.eventmanagement.model.EventVendor;
import com.eventmanagement.model.Event;
import java.util.List;

public class VendorService {
    private final VendorDAO vendorDAO = new VendorDAO();

    public boolean createVendor(Vendor v) { return vendorDAO.createVendor(v); }
    public boolean updateVendor(Vendor v) { return vendorDAO.updateVendor(v); }
    public boolean deleteVendor(int vendorId) { return vendorDAO.deleteVendor(vendorId); }
    public List<Vendor> getAllVendors() { return vendorDAO.getAllVendors(); }

    public boolean assignVendorToEvent(EventVendor ev) { return vendorDAO.assignVendorToEvent(ev); }
    public boolean unassignVendorFromEvent(int eventId, int vendorId) { return vendorDAO.unassignVendorFromEvent(eventId, vendorId); }

    public List<Event> getEventsForVendor(int vendorId) { return vendorDAO.getEventsForVendor(vendorId); }

    public List<String> getAssignedEventNamesForVendor(int vendorId) {
        return vendorDAO.getEventsForVendor(vendorId).stream().map(Event::getEventName).toList();
    }
}