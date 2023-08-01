import { useMutation, useQuery } from '@tanstack/react-query';
import Button from '../../../components/Button';
import { RewardContainer, RewardContent, RewardItemContainer, RewardItemWrapper } from './style';
import Text from '../../../components/Text';
import { useLocation, useNavigate } from 'react-router-dom';
import { Spacing } from '../../../style/layout/common';
import { getCustomer, getReward } from '../../../api/get';
import { patchReward } from '../../../api/patch';
import { ROUTER_PATH } from '../../../constants';
import { RewardRes } from '../../../types/api';

const RewardPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const phoneNumber = location.state.phoneNumber;

  const { data: customerData } = useQuery(['getCustomer', phoneNumber], () =>
    getCustomer(phoneNumber),
  );
  const { data: rewardData, status } = useQuery(
    ['getReward', customerData],
    // TODO: cafeId 전역으로 받아오기
    () => getReward(customerData.customer[0].id, 1),
    {
      enabled: !!customerData,
    },
  );

  const { mutate: mutateReward } = useMutation({
    mutationFn: (rewardId: number) => patchReward(customerData.customer[0].id, rewardId),
    onSuccess() {
      navigate(ROUTER_PATH.customerList);
    },
    onError() {
      alert('에러가 발생했습니다. 네트워크 상태를 확인해주세요.');
    },
  });

  const activateRewardButton = (rewardId: number) => {
    mutateReward(rewardId);
  };

  if (status === 'error') {
    return <div>불러오는 중 에러가 발생했습니다. 다시 시도해주세요.</div>;
  }

  if (status === 'loading') {
    return <div>고객 정보 불러오는 중...</div>;
  }

  return (
    <>
      <Spacing $size={40} />
      <Text variant="pageTitle">리워드 사용</Text>
      <Spacing $size={36} />
      <RewardContainer>
        <Text variant="pageTitle">{customerData.customer[0].nickname}고객님</Text>
        <Spacing $size={72} />
        <Text variant="subTitle">보유 리워드 내역</Text>
        <Spacing $size={42} />
        <RewardItemContainer>
          {rewardData.rewards.length ? (
            rewardData.rewards.map(({ id, name }: RewardRes) => (
              <RewardItemWrapper key={id}>
                <RewardContent>{name}</RewardContent>
                <Button onClick={() => activateRewardButton(id)}>사용</Button>
              </RewardItemWrapper>
            ))
          ) : (
            <p>보유한 리워드가 없습니다.</p>
          )}
        </RewardItemContainer>
      </RewardContainer>
    </>
  );
};

export default RewardPage;
