package com.sa98077.sp_boot_board_prac.service;

import com.sa98077.sp_boot_board_prac.domain.Board;
import com.sa98077.sp_boot_board_prac.dto.BoardDTO;
import com.sa98077.sp_boot_board_prac.dto.PageRequestDTO;
import com.sa98077.sp_boot_board_prac.dto.PageResponseDTO;
import com.sa98077.sp_boot_board_prac.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional // 트랜잭션 가져올때 자카르타 가져오기 주의...
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;

    @Override
    public Long add(BoardDTO boardDTO) {
        Board board = modelMapper.map(boardDTO, Board.class);
        return boardRepository.save(board).getBno();
    }

    @Override
    public BoardDTO searchOne(Long bno) {
        Board board = boardRepository.findById(bno).orElseThrow();
        return modelMapper.map(board, BoardDTO.class);
    }

    @Override
    public void modify(BoardDTO boardDTO) {
        Board board = boardRepository.findById(boardDTO.getBno()).orElseThrow();
        board.change(boardDTO.getTitle(), boardDTO.getContent());
        boardRepository.save(board);
    }

    @Override
    public void remove(Long bno) {
        boardRepository.deleteById(bno);
    }

    @Override
    public PageResponseDTO<BoardDTO> getList(PageRequestDTO pageRequestDTO) {

        // 1. 검색 조건 및 페이징 정보 설정
        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("bno");

        // 2. 레포지토리 호출 및 결과 처리
        Page<Board> result = boardRepository.searchAll(types, keyword, pageable);

        // 고전적인 변경방법.

//        List<BoardDTO> dtoList = new ArrayList<>();
//        for (Board board : result.getContent()) {
//            dtoList.add(modelMapper.map(board, BoardDTO.class));
//        }

        // 트랜드 1
//
//        List<BoardDTO> dtoList = result
//                .stream()
//                .map(board -> modelMapper.map(board, BoardDTO.class))
//                .collect(Collectors.toList());

        // 간략화
//        List<BoardDTO> dtoList = result
//                .stream()
//                .map(board -> modelMapper.map(board, BoardDTO.class))
//                .toList();

        // 더 간략화
        List<BoardDTO> dtoList = result
                .map(board -> modelMapper.map(board, BoardDTO.class))
                .getContent();



        // 3. PageResponseDTO 생성
        PageResponseDTO<BoardDTO> pageResponseDTO = PageResponseDTO.<BoardDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(result.getTotalPages()) // 전체 개수 기준으로 총 페이지 계산
                .build();
        return pageResponseDTO;
    }
}
