package Service;

import Domain.Booking;
import Domain.BookingValidatorException;
import Domain.Client;
import Domain.Movie;
import Repository.IRepository;

import java.awt.print.Book;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingService {
    private IRepository < Booking > bookingRepository;
    private IRepository < Client > clientRepository;
    private IRepository < Movie > movieRepository;

    public BookingService(IRepository < Booking > bookingRepository, IRepository < Client > clientRepository, IRepository < Movie > movieRepository) {
        this.bookingRepository = bookingRepository;
        this.movieRepository = movieRepository;
        this.clientRepository = clientRepository;

    }

    /**
     * inserts a booking if movie is in program;
     * puts 10% bonus points of movie price to an existing client card;
     * @param id
     * @param idMovie
     * @param idClient
     * @param date
     * @param time
     * @throws BookingServiceException if movie is not on screens or movie id doesn't exist
     */

    public void insert(String id, String idMovie, String idClient, LocalDate date, LocalTime time) {
        Movie soldMovie = movieRepository.getById(idMovie);
        if (soldMovie == null) {
            throw new BookingServiceException( "There is no movie with given id" );
        }
        if (!soldMovie.isOnScreens()) {
            throw new BookingServiceException( "The movie is not on screen!" );
        }
        Booking booking = new Booking( id, idMovie, idClient, date, time );
        bookingRepository.insert( booking );
        movieRepository.getById(idMovie).setBookings(movieRepository.getById(idMovie).getBookings() + 1);

        Client client = clientRepository.getById( idClient );
        if (client != null) {
            client.setBonusPoints( (int) ( client.getBonusPoints() + ( soldMovie.getPrice() / 10 ) ) );
        }
    }

    /**
     * updates a booking
     *
     * @param id
     * @param idMovie
     * @param idClient
     * @param date
     * @param time
     */
    public void update(String id, String idMovie, String idClient, LocalDate date, LocalTime time) {
        Booking booking = new Booking( id, idMovie, idClient, date, time );
        bookingRepository.update( booking );
    }

    /**
     * deletes a booking from id;
     *
     * @param id
     */
    public void remove(String id) {
        bookingRepository.remove( id );
    }

    /**
     * list of all bookings;
     *
     * @return an ArrayList containing all bookings;
     */
    public List < Booking > getAll() {
        return bookingRepository.getAll();
    }

    public List < Booking > fullTextSearch(String text) {
        List < Booking > found = new ArrayList <>();
        for (Booking b : bookingRepository.getAll()) {
            if (( b.getId().contains( text ) ) ||
                    ( b.getIdMovie().contains( text ) ) ||
                    ( b.getIdClient().contains( text ) ) ||
                    ( b.getDate().toString().contains( text ) ) ||
                    ( b.getTime().toString().contains( text ) ) &&
                            !found.contains( b )) {
                found.add( b );
            }
        }
        return found;
    }

    public List< Booking> bookingsByPeriod(LocalTime begin, LocalTime end) {
        List <Booking> bookings = new ArrayList <>();
        for (Booking b : bookingRepository.getAll()) {
            if (b.getTime().isAfter( begin ) && b.getTime().isBefore( end )) {
                bookings.add( b );
            }
        }
        return bookings;
    }

    public void removeBookingsByPeriod(LocalDate begin, LocalDate end) {
        for (Booking b : bookingRepository.getAll()) {
            if(b.getDate().isAfter(begin) && b.getDate().isBefore(end)){
                bookingRepository.remove(b.getId());
            }
        }
    }
}
