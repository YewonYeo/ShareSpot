package com.sharespot.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sharespot.entity.Comment;
import com.sharespot.entity.User;
import com.sharespot.repo.CommentRepository;
import com.sharespot.repo.UserRepository;
import com.sharespot.service.UserService;

@Slf4j
@RestController
@RequestMapping("/main/posts")
public class CommentController {
	
	@Autowired
	private CommentRepository commentRepository;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;

	@GetMapping("/main/posts/{postNo}")
	@ApiOperation(value = "게시글 상세조회", notes = "해당 게시물의 <b>댓글목록</b>을 반환한다.")
	public ResponseEntity<List<Comment>> getComments(@PathVariable int postNo){
		
		List<Comment> comments = commentRepository.findByPostId(postNo);
		
		return new ResponseEntity<>(comments, HttpStatus.OK);
	}
	
	@PostMapping("/{postNo}/comments")
	@ApiOperation(value = "댓글작성", notes = "해당 postNo의 게시물에 <b>댓글</b>을 작성한다.")
	public ResponseEntity<Integer> createComment(@RequestBody Comment comment, @PathVariable int postNo){
		
		User user = userService.getUser(comment.getUserId()).get();
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		System.out.println(sdf.format(timestamp));
		
		Comment commentEntity = Comment.builder()
				.postId(postNo)
				.userId(comment.getUserId())
				.comment(comment.getComment())
				.userImage(user.getProfileImage())
				.userNick(user.getNickname())
				.uploadTime(sdf.format(timestamp))
				.build();
		
		int result = 1;
		try {
			commentRepository.save(commentEntity);
		} catch (Exception e) {
			result = 0;
		}
				
		return new ResponseEntity<Integer>(result, HttpStatus.OK);
	}
	
	@PutMapping("/{postNo}/comments/{commentNo}")
	@ApiOperation(value = "댓글수정", notes = "<b>댓글</b>을 수정한다.")
	public ResponseEntity<Integer> updateComment(@RequestBody Comment comment, @PathVariable int postNo,int commentNo){
		Optional<Comment> option = commentRepository.findById(commentNo);
		int result = 0;
		if(option.isPresent()) {
			Comment c = option.get();
			
			c.setComment(comment.getComment());
			commentRepository.save(c);
			
			result = 1;
		}
		
		return new ResponseEntity<Integer>(result, HttpStatus.OK);
	}
	
	@DeleteMapping("/{postNo}/comments/{commentNo}")
	@ApiOperation(value = "댓글삭제", notes = "<b>댓글</b>을 삭제한다.")
	public ResponseEntity<Integer> deleteComment(@PathVariable int postNo, @PathVariable int commentNo){
		int result = 0;
		if(commentRepository.findById(commentNo).isPresent()) {
			
			
			
			commentRepository.deleteById(commentNo);
			result = 1;
		}
		return new ResponseEntity<Integer>(result, HttpStatus.OK);
	}
	
}
