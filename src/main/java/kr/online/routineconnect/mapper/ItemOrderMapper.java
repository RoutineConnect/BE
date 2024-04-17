package kr.online.routineconnect.mapper;

import kr.online.routineconnect.domain.ItemOrder;
import kr.online.routineconnect.domain.ItemOrderIgnore;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ItemOrderMapper {

    ItemOrderMapper INSTANCE = Mappers.getMapper(ItemOrderMapper.class);

    @Mapping(target = "id", source = "itemOrder.id")
    @Mapping(target = "itemOrder", source = "itemOrder")
    @Mapping(target = "user", source = "itemOrder.user")
    @Mapping(target = "date", source = "itemOrder.date")
    @Mapping(target = "day", source = "itemOrder.day")
    ItemOrderIgnore toIgnore(ItemOrder itemOrder);
}