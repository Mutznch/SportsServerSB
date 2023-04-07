package br.pucpr.sportsserver.rest.comments;

import br.pucpr.sportsserver.lib.exception.BadRequestException;
import br.pucpr.sportsserver.lib.exception.NotFoundException;
import br.pucpr.sportsserver.rest.comments.request.CommentRequest;
import br.pucpr.sportsserver.rest.comments.response.CommentResponse;
import br.pucpr.sportsserver.rest.users.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CommentsService {
    CommentsRepository commentsRepository;
    UsersRepository usersRepository;

    public CommentsService(CommentsRepository commentsRepository, UsersRepository usersRepository) {
        this.commentsRepository = commentsRepository;
        this.usersRepository = usersRepository;
    }

    private Comment reqToComment(CommentRequest reqComment) {
        return new Comment(
                reqComment.getText(),
                reqComment.getRating()
        );
    }

    private void commentExists(String from, String to) {
        Set<Comment> fromComments = commentsRepository.findAllByFromUsername(from);
        Set<Comment> toComments = commentsRepository.findAllByToUsername(to);
        fromComments.retainAll(toComments);
        if (!(fromComments.isEmpty())) throw new BadRequestException("Comment already exists");
    }

    private CommentResponse commentToRes(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getFrom().getUsername(),
                comment.getTo().getUsername(),
                comment.getRating(),
                comment.getText()
        );
    }

    public Set<CommentResponse> search(Long id, String from, String to) {
        Set<CommentResponse> resComments = new HashSet<>();
        Set<Comment> comments = new HashSet<>();
        if (id != null)
            comments.add(commentsRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found")));
        else if (from != null && to != null)
            comments.add(commentsRepository.findByFromUsernameAndToUsername(from, to)
                    .orElseThrow(() -> new NotFoundException("Comment Not Found")));
        else if (from != null)
            comments.addAll(commentsRepository.findAllByFromUsername(from));
        else if (to != null)
            comments.addAll(commentsRepository.findAllByToUsername(to));
        else
            comments.addAll(commentsRepository.findAll());

        if (comments.isEmpty()) throw new NotFoundException("Comments Not Found");
        comments.forEach(c -> resComments.add(commentToRes(c)));
        return resComments;
    }

    public CommentResponse add(CommentRequest reqComment, String fromUsername, String toUsername) {
        if (fromUsername == toUsername) throw new BadRequestException("Can't comment on yourself");
        commentExists(fromUsername, toUsername);
        var comment = reqToComment(reqComment);
        var from = usersRepository.findByUsername(fromUsername)
                .orElseThrow(() -> new BadRequestException("User \"" + fromUsername + "\" Not Found"));
        var to = usersRepository.findByUsername(toUsername)
                .orElseThrow(() -> new BadRequestException("User \"" + toUsername + "\" Not Found"));
        comment.addUsers(from, to);

        return commentToRes(commentsRepository.save(comment));
    }

    public void deleteById(Long id) {
        var comment = commentsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment Not Found"));
        comment.removeUsers();
        commentsRepository.save(comment);
        commentsRepository.deleteById(id);
    }

    public void delete(String from, String to) {
        Long id = search(null, from, to).stream().toList().get(0).getId();
        deleteById(id);
    }

    public Double searchUserRating(String username) {
        List<Double> sum = new ArrayList<>();
        search(null, null, username).forEach(c -> sum.add(c.getRating().doubleValue()));
        var divisor = sum.size();
        if (divisor != 0) return (double) Math.round(
                ((sum.stream().reduce(0d, (a, b) -> a + b))/divisor)*100)/100;
        else throw new NotFoundException("User wasn't rated yet");
    }

    public CommentResponse update(CommentRequest reqComment, String from, String to) {
        List<CommentResponse> resComment = search(null, from, to).stream().toList();
        if (resComment.isEmpty()) throw new NotFoundException("Comment Not Found");
        var comment = commentsRepository.findById(resComment.get(0).getId())
                .orElseThrow(() -> new NotFoundException("Comment Not Found"));
        comment.setText(reqComment.getText());
        comment.setRating(reqComment.getRating());
        return commentToRes(commentsRepository.save(comment));
    }
}
