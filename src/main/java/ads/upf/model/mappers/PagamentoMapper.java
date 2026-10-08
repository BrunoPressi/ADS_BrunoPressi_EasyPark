package ads.upf.model.mappers;

import ads.upf.model.DTOs.pagamento.PagamentoCreateDTO;
import ads.upf.model.DTOs.pagamento.PagamentoResponseDTO;
import ads.upf.model.entities.Pagamento;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface PagamentoMapper {

    PagamentoMapper INSTANCE = Mappers.getMapper( PagamentoMapper.class );

    Pagamento toPagamento(PagamentoCreateDTO pagamentoCreateDTO);
    PagamentoResponseDTO toPagamentoDto(Pagamento pagamento);

}
