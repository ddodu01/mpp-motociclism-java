package ro.mpp2024.repository;

import ro.mpp2024.Participant;

public interface ParticipantRepository extends Repository<Long, Participant> {
    // cautarea participantilor dupa echipa
    Iterable<Participant> findByEchipa(String echipa);
}