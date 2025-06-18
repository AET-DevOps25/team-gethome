package com.message_service.service;


import com.message_service.client.UserMgmtClient;
import com.message_service.dto.*;
import com.message_service.model.IssuedBy;
import com.message_service.model.Location;
import com.message_service.model.PreferredContactMethod;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserMgmtClient userMgmtClient;
    private final EmailService emailService;

    public boolean notifyJourneyStart(String authHeader, String userId, JourneyStartRequest request) {
        UserProfileMinimalDTO profile = userMgmtClient.getUserById(userId, authHeader);
        if (profile == null) {
            return false; // User not found
        }
        List<EmergencyContactDTO> contacts = profile.getEmergencyContacts();
        for(EmergencyContactDTO contact : contacts) {
            String message = formatStartMessage(
                    request.getStartLocation(),
                    request.getEndLocation(),
                    request.getLengthInMeters(),
                    profile.getAlias(),
                    contact.getName()
            );
            try {
                dispatch(contact, message, "Journey Started for " + profile.getAlias());
            } catch (MessagingException e) {
                return false; // Failed to send email
            }
        }
        return true;
    }

    public boolean notifyJourneyEnd(String authHeader, String userId, JourneyEndRequest request) {
        UserProfileMinimalDTO profile = userMgmtClient.getUserById(userId, authHeader);
        if (profile == null) {
            return false; // User not found
        }
        List<EmergencyContactDTO> contacts = profile.getEmergencyContacts();
        for(EmergencyContactDTO contact : contacts) {
            String message = formatEndMessage(
                    request.endLocation(),
                    request.notes(),
                    profile.getAlias(),
                    contact.getName()
            );
            try {
                dispatch(contact, message, "Journey Ended for " + profile.getAlias());
            } catch (MessagingException e) {
                return false; // Failed to send email
            }
        }
        return true;
    }

    public boolean notifyEmergency(String authHeader, String userId, EmergencyNotificationRequest request) {
        UserProfileMinimalDTO profile = userMgmtClient.getUserById(userId, authHeader);
        if (profile == null) {
            return false; // User not found
        }
        List<EmergencyContactDTO> contacts = profile.getEmergencyContacts();
        for(EmergencyContactDTO contact : contacts) {
            String message = formatEmergencyMessage(
                    request.getLocation(),
                    request.getIssuedBy(),
                    request.getContext(),
                    profile.getAlias(),
                    contact.getName()
            );
            try {
                dispatch(contact, message, "Emergency Alert for " + profile.getAlias());
            } catch (MessagingException e) {
                return false;
            }
        }
        return true;
    }

    private void dispatch(EmergencyContactDTO contact, String message, String subject) throws MessagingException {
        if (contact.getPreferredMethod() == PreferredContactMethod.EMAIL) {
           emailService.sendEmail(contact.getEmail(), subject, message);
        } else if (contact.getPreferredMethod() == PreferredContactMethod.SMS) {
            // Not implemented yet
            throw new UnsupportedOperationException("SMS notifications are not implemented yet.");
        }
    }

    private String formatStartMessage(
            Location startLocation,
            Location endLocation,
            double lengthInMeters,
            String aliasSender,
            String aliasRecipient
    ) {
        return String.format(
                """
                Hello %s!
                Your friend %s started his journey home.
                You can look at the following link to get a rough estimation of the travel!
                https://www.google.de/maps/dir/%f,%f/%f,%f/
                In total your friend will have to travel %s m.
                We will notify you when your friend arrives home safely.
                
                Greetings,
                GetHome
                """,
                aliasRecipient,
                aliasSender,
                startLocation.getLatitude(),
                startLocation.getLongitude(),
                endLocation.getLatitude(),
                endLocation.getLongitude(),
                lengthInMeters
        );
    }

    private String formatEndMessage(
            Location endLocation,
            String notes,
            String aliasSender,
            String aliasRecipient
    ) {
        return String.format(
                """
                Hello %s!
                Your friend %s arrived or ended the journey.
                You can look at the following link to get a rough estimation where the end was at!
                https://www.google.de/maps/place/%f+%f/
        
                Your friend left the following message:
                %s
    
                Seems off? Better reach out!
    
                Greetings,
                GetHome
                """,
                aliasRecipient,
                aliasSender,
                endLocation.getLatitude(),
                endLocation.getLongitude(),
                notes
        );
    }

    private String formatEmergencyMessage(
            Location location,
            IssuedBy issuedBy,
            String context,
            String aliasSender,
            String aliasRecipient
    ) {
        String detectedBy = "";
        if (issuedBy == IssuedBy.AI) {
            detectedBy = "detected by our AI-companion";
        } else if (issuedBy == IssuedBy.USER) {
            detectedBy = "flagged by " + aliasSender;
        }

        String contextMessage = context == null ? "" :
                "\nFor you to better understand the situation, here is why our companion detected the emergency:\n" +
                context;

        return String.format(
                """
                Hello %s!
                Your friend %s sadly had an emergency.
                Please reach out! %s probably needs help.
                This is the last location we know of:
                https://www.google.de/maps/place/%f+%f
                The emergency was %s.
                %s
         
                Greetings,
                GetHome
                """,
                aliasRecipient,
                aliasSender,
                aliasSender,
                location.getLatitude(),
                location.getLongitude(),
                detectedBy,
                contextMessage
        );

    }
}
